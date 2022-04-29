package com.blackmorse.telegrambotenglish.akka.states

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.ShowCommandsEvent
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage

class HelloScreenState(userData: UserData) : State(userData) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<TelegramMessage, Event, State>
    ): Effect<Event, State> {
        return behavior.Effect().persist(ShowCommandsEvent).thenRun { englishBot.sendCommandsList(userData.chatId)}
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return ShowCommandsState(userData)
    }

    override fun runOnBack(englishBot: EnglishBot) {
        englishBot.sendCommandsList(userData.chatId)
    }

    override fun backState(): State {
        return this
    }
}