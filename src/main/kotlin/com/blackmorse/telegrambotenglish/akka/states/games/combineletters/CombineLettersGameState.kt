package com.blackmorse.telegrambotenglish.akka.states.games.combineletters

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.blackmorse.telegrambotenglish.akka.states.ShowDictionaryState
import com.blackmorse.telegrambotenglish.akka.states.State
import com.blackmorse.telegrambotenglish.akka.states.games.GameData
import com.blackmorse.telegrambotenglish.akka.states.games.GameState
import com.fasterxml.jackson.annotation.JsonProperty

data class CorrectLetterSelectedEvent(
    @JsonProperty("letter")
    val letter: Char
) : Event

object IncorrectLetterSelectedEvent : Event

class CombineLettersGameState(userData: UserData, val dictionary: Dictionary,
                              val gameData: CombineLettersGameData, override val chainGamesData: List<GameData>) : State(userData), GameState {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<TelegramMessage, Event, State>
    ): Effect<Event, State> {
        val message = msg.update.message.text
        return if (message.length > 1) {
            behavior.Effect().none()
                .thenRun{ englishBot.justSendText("Irrelevant input", userData.chatId) }
        } else {
            val char = message[0]
            val startOfString = gameData.selectedChars.joinToString("") + char
            if (gameData.word.translation.startsWith(startOfString)) {
                behavior.Effect().persist(CorrectLetterSelectedEvent(char))
                    .thenRun { state: State -> state.sendBeforeStateMessage(englishBot) }
            } else {
                behavior.Effect().persist(IncorrectLetterSelectedEvent)
                    .thenRun { state: State ->
                        englishBot.justSendText("Wrong letter!", userData.chatId)
                        state.sendBeforeStateMessage(englishBot)
                    }
            }
        }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when (clazz) {
            IncorrectLetterSelectedEvent::class.java -> this
            CorrectLetterSelectedEvent::class.java -> {
               val letter = (event as CorrectLetterSelectedEvent).letter
               return if (gameData.selectedChars.joinToString("") + letter == gameData.word.translation) {
                   if (chainGamesData.isEmpty()) {
                       ShowDictionaryState(userData, dictionary)
                   } else {
                       chainGamesData[0].createState(userData, dictionary, chainGamesData - chainGamesData[0])
                   }
               } else {
                   val newGameData = CombineLettersGameData(gameData.word, gameData.mixedLetters - letter, gameData.selectedChars + letter)
                   CombineLettersGameState(userData, dictionary, newGameData, chainGamesData)
               }
            }
            else -> this
        }
    }

    override fun sendBeforeStateMessage(englishBot: EnglishBot) {
        englishBot.sendCombineLettersGameState(userData.chatId, gameData)
    }

    override fun backState(): State {
        return ShowDictionaryState(userData, dictionary)
    }
}