package com.blackmorse.telegrambotenglish.akka.states

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.blackmorse.telegrambotenglish.akka.messages.UserActorMessage
import com.fasterxml.jackson.annotation.JsonProperty

object ShowCommandsEvent: Event

class HelloScreenState(
    @JsonProperty("userData")
    userData: UserData) : State(userData) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<UserActorMessage, Event, State>
    ): Effect<Event, State> {
        return behavior.Effect().persist(ShowCommandsEvent).thenRun { state: ShowCommandsState -> state.sendBeforeStateMessage(englishBot) }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return ShowCommandsState(userData)
    }

    override fun backState(): State {
        return this
    }

    override fun sendBeforeStateMessage(englishBot: EnglishBot) {}
}
