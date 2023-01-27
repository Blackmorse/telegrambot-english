package com.blackmorse.telegrambotenglish.akka.states.games.combineletters

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.blackmorse.telegrambotenglish.akka.messages.UserActorMessage
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

class CombineLettersGameState(userData: UserData,
                              gameData: CombineLettersGameData,
                              chainGamesData: List<GameData>,
                              stateAfterFinish: State) : GameState<CombineLettersGameData>(userData, gameData, chainGamesData, stateAfterFinish) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<UserActorMessage, Event, State>
    ): Effect<Event, State> {
        val message = msg.update.message.text
        return if (message.length > 1) {
            behavior.Effect().none()
                .thenRun{
                    englishBot.justSendText("Irrelevant input", userData.chatId)
                    sendBeforeStateMessage(englishBot)
                }
        } else {
            val char = if (message[0] == '_') ' ' else message[0]
            val startOfString = gameData.selectedChars.joinToString("") + char
            if (gameData.word.translation.startsWith(startOfString)) {
                behavior.Effect().persist(CorrectLetterSelectedEvent(char))
                    .thenRun { state: State -> state.sendBeforeStateMessage(englishBot) }
            } else if(char == '\u2705') {
                behavior.Effect().none()
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
                   finishGame()
               } else {
                   val index = gameData.mixedLetters.indexOf(letter)
                   val newMixedLetters = gameData.mixedLetters.mapIndexed{i, let -> if (i == index) '+' else let}

                   val newGameData = gameData.copy(mixedLetters = newMixedLetters, selectedChars = gameData.selectedChars + letter)

                   CombineLettersGameState(userData, newGameData, chainGamesData, stateAfterFinish)
               }
            }
            else -> this
        }
    }

    override fun sendBeforeStateMessage(englishBot: EnglishBot) {
        englishBot.sendCombineLettersGameState(userData.chatId, gameData)
    }
}