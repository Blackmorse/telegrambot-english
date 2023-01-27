package com.blackmorse.telegrambotenglish.akka.states

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.Commands
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.blackmorse.telegrambotenglish.akka.messages.UserActorMessage
import com.blackmorse.telegrambotenglish.akka.states.games.combineletters.CombineLettersGameData
import com.blackmorse.telegrambotenglish.akka.states.games.typetranslation.TypeTranslationGameData
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*
import kotlin.random.Random

object ShowDictionariesEvent: Event

data class WordOfTheDayEvent(@JsonProperty("seed") val seed: Long) : Event

class ShowCommandsState(
    @JsonProperty("userData")
    userData: UserData) : State(userData) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<UserActorMessage, Event, State>
    ): Effect<Event, State> {
        return if (msg.update.message.text == Commands.SHOW_DICTIONARIES.text) {
            behavior.Effect().persist(ShowDictionariesEvent)
                    .thenRun { state: State -> state.sendBeforeStateMessage(englishBot) }
        } else if (msg.update.message.text == Commands.WORD_OF_THE_DAY.text) {
            behavior.Effect().persist(WordOfTheDayEvent(System.nanoTime()))
                    .thenRun { state: State -> state.sendBeforeStateMessage(englishBot) }
        } else {
            behavior.Effect().none().thenNoReply()
        }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when (clazz) {
            ShowDictionariesEvent::class.java -> ShowDictionariesState(userData)
            WordOfTheDayEvent::class.java -> {
                val seed = (event as WordOfTheDayEvent).seed
                val random = Random(seed)
                val allWords = userData.dictionaries.flatMap { it.words }

//                if (allWords.isEmpty()) {
//                    ShowCommandsState(userData)
//                } else {
//                    val wordNumber = random.nextInt(allWords.size)
//                    val word = allWords[wordNumber]
//
//                    val gameDatas = listOf(
//                            TypeTranslationGameData.init(word),
//                            TypeTranslationGameData.reverseInit(word),
//                            CombineLettersGameData.init(word, random),
//                            CombineLettersGameData.reverseInit(word, random)
//                    ).shuffled(random)
//
//                    gameDatas[0].createState(userData, gameDatas - gameDatas[0], this)
//
                    val word = if (allWords.isEmpty()) {
                        Optional.empty()
                    } else {
                        val wordNumber = random.nextInt(allWords.size)
                        Optional.of(allWords[wordNumber])
                    }

                    WordOfDayState(userData, this, word, seed)
//                }
            }
            else -> this
        }
    }

    override fun backState(): State {
        return this
    }

    override fun sendBeforeStateMessage(englishBot: EnglishBot) {
        englishBot.sendCommandsList(userData.chatId)
    }
}
