package com.blackmorse.telegrambotenglish.akka.states

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.WordWithTranslation
import com.blackmorse.telegrambotenglish.akka.messages.Commands
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.blackmorse.telegrambotenglish.akka.messages.UserActorMessage
import com.blackmorse.telegrambotenglish.akka.states.games.GameData
import com.blackmorse.telegrambotenglish.akka.states.games.combineletters.CombineLettersGameData
import com.blackmorse.telegrambotenglish.akka.states.games.fourchoices.FourChoicesGameData
import com.blackmorse.telegrambotenglish.akka.states.games.twocolumns.TwoColumnsGameData
import com.blackmorse.telegrambotenglish.akka.states.games.twocolumns.TwoColumnsGameState
import com.blackmorse.telegrambotenglish.akka.states.games.typetranslation.TypeTranslationGameData
import com.fasterxml.jackson.annotation.JsonProperty
import kotlin.math.min
import kotlin.random.Random

object AddWordEvent : Event
object DeleteWordEvent : Event
data class StartGameEvent(@JsonProperty("seed") val seed: Long) : Event

class ShowDictionaryState(
    @JsonProperty("userData")
    userData: UserData,
    @JsonProperty("dictionary")
    val dictionary: Dictionary) : State(userData) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<UserActorMessage, Event, State>
    ): Effect<Event, State> {
        return when (msg.update.message.text) {
            Commands.ADD_WORD.text -> {
                behavior.Effect().persist(AddWordEvent)
                    .thenRun{ state: AddWordToDictionaryState -> state.sendBeforeStateMessage(englishBot) }
            }
            Commands.DELETE_WORD.text -> {
                behavior.Effect().persist(DeleteWordEvent)
                    .thenRun { state: DeleteWordFromDictionaryState -> state.sendBeforeStateMessage(englishBot) }
            }
            Commands.START_GAME.text -> {
                behavior.Effect().persist(StartGameEvent(System.nanoTime()))
                    .thenRun { state: State -> state.sendBeforeStateMessage(englishBot) }
            }
            else -> {
                behavior.Effect().none().thenNoReply()
            }
        }
    }

    private fun createTwoColumnGames(dictionary: Dictionary, random: Random): List<GameData> {
        val gamesData = mutableListOf<TwoColumnsGameData>()
        val wordsSet = mutableSetOf<WordWithTranslation>()

        while(wordsSet.size < min(dictionary.words.size, dictionary.words.size / 4)) {
            val gameData = TwoColumnsGameData.init(dictionary, random)
            wordsSet.addAll(gameData.words)
            gamesData.add(gameData)
        }

        wordsSet.clear()
        while(wordsSet.size < min(dictionary.words.size, dictionary.words.size / 4)) {
            val gameData = TwoColumnsGameData.reverseInit(dictionary, random)
            wordsSet.addAll(gameData.words)
            gamesData.add(gameData)
        }

        return gamesData
    }

    private fun createTypeTranslationGames(dictionary: Dictionary, random: Random): List<GameData> {
        val words = dictionary.words.shuffled(random)
        val firstPart = words.slice(IntRange(0, words.size / 2))
        val secondPart = words - firstPart

        return firstPart
            .map { TypeTranslationGameData.init(it) } +
                secondPart
                .map { TypeTranslationGameData.reverseInit(it) }
    }

    private fun createFourChoicesGameData(dictionary: Dictionary, random: Random): List<GameData> {
        return dictionary.words
            .map { FourChoicesGameData.init(dictionary, it, random) } +
            dictionary.words
                .map { FourChoicesGameData.reverseInit(dictionary, it, random) }
    }

    private fun createCombineLettersGameData(dictionary: Dictionary, random: Random): List<GameData> {
        val words = dictionary.words.shuffled(random)
        val firstPart = words.slice(IntRange(0, words.size / 2))
        val secondPart = words - firstPart

        return firstPart
            .map { CombineLettersGameData.init(it, random) } +
            secondPart
                .map { CombineLettersGameData.reverseInit(it, random) }
    }

    private fun createGames(dictionary: Dictionary, random: Random): List<GameData> {
        if (dictionary.words.isEmpty()) {
            return emptyList()
        }
        return (createTwoColumnGames(dictionary, random) +
            createTypeTranslationGames(dictionary, random) +
            createFourChoicesGameData(dictionary, random) +
            createCombineLettersGameData(dictionary, random)).shuffled(random)
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when (clazz) {
            AddWordEvent::class.java -> AddWordToDictionaryState(userData, dictionary)
            DeleteWordEvent::class.java -> DeleteWordFromDictionaryState(userData, dictionary)
            StartGameEvent::class.java -> {
                val seed = (event as StartGameEvent).seed
                val random = Random(seed)
                val datas = createGames(dictionary, random)
                if (datas.isEmpty()) {
                    this
                } else {
                    datas[0].createState(userData, datas - datas[0], ShowDictionaryState(userData, dictionary))
                }
            }
            else -> this
        }
    }

    override fun sendBeforeStateMessage(englishBot: EnglishBot) {
        englishBot.sendDictionaryInfo(userData.chatId, dictionary)
    }

    override fun backState(): State {
        return ShowDictionariesState(userData)
    }
}
