package com.blackmorse.telegrambotenglish.akka.states.games.twocolumns

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.blackmorse.telegrambotenglish.akka.messages.UserActorMessage
import com.blackmorse.telegrambotenglish.akka.states.State
import com.blackmorse.telegrambotenglish.akka.states.games.GameData
import com.blackmorse.telegrambotenglish.akka.states.games.GameState
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class LeftColumnSelectedEvent(
    @JsonProperty("selected")
    val selected: String) : Event

class TwoColumnsGameState(userData: UserData,
                          gameData: TwoColumnsGameData,
                          chainGamesData: List<GameData>,
                          stateAfterFinish: State) : GameState<TwoColumnsGameData>(userData, gameData, chainGamesData, stateAfterFinish) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<UserActorMessage, Event, State>
    ): Effect<Event, State> {
        return if (gameData.leftColumn.contains(msg.update.message.text)) {
            behavior.Effect().persist(LeftColumnSelectedEvent(msg.update.message.text))
                .thenRun { state: TwoColumnsGameLeftColumnSelectedState -> state.sendBeforeStateMessage(englishBot) }
        } else {
            behavior.Effect().none().thenNoReply()
        }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when (clazz) {
            LeftColumnSelectedEvent::class.java ->
                TwoColumnsGameLeftColumnSelectedState(userData,
                        gameData.copy(leftSelectedWord = Optional.of((event as LeftColumnSelectedEvent).selected)),
                        chainGamesData,
                        stateAfterFinish)
            else -> this
        }
    }

    override fun sendBeforeStateMessage(englishBot: EnglishBot) {
        englishBot.sendTwoColumnsGame(userData.chatId, gameData)
    }
}
