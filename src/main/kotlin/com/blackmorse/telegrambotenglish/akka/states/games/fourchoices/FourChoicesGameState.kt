package com.blackmorse.telegrambotenglish.akka.states.games.fourchoices

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

data class CorrentTranslationSelectedEvent(
    @JsonProperty("translation")
    val translation: String
) : Event

object WrongTranslationSelectedEvent : Event

class FourChoicesGameState(userData: UserData,
                           gameData: FourChoicesGameData,
                           chainGamesData: List<GameData>,
                           stateAfterFinish: State) : GameState<FourChoicesGameData>(userData, gameData, chainGamesData, stateAfterFinish) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<UserActorMessage, Event, State>
    ): Effect<Event, State> {
        val attempt = msg.update.message.text
        return if (attempt == gameData.word.translation) {
            behavior.Effect().persist(CorrentTranslationSelectedEvent(attempt))
                .thenRun { state: State -> state.sendBeforeStateMessage(englishBot) }
        } else {
            behavior.Effect().persist(WrongTranslationSelectedEvent)
                .thenRun { state: State ->
                    englishBot.justSendText("Wrong answer!", userData.chatId)
                    state.sendBeforeStateMessage(englishBot)
                }
        }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when (clazz) {
            WrongTranslationSelectedEvent -> this
            CorrentTranslationSelectedEvent::class.java -> finishGame()
            else -> this
        }
    }

    override fun sendBeforeStateMessage(englishBot: EnglishBot) {
        englishBot.sendFourChoicesGame(userData.chatId, gameData)
    }
}