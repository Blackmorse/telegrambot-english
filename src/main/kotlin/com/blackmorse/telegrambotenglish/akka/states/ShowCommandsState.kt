package com.blackmorse.telegrambotenglish.akka.states

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.ShowDictionariesEvent
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.Commands
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage

class ShowCommandsState(userData: UserData) : State(userData) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<TelegramMessage, Event, State>
    ): Effect<Event, State> {
        return if (msg.update.message.text == Commands.SHOW_DICTIONARIES.text) {
            behavior.Effect().persist(ShowDictionariesEvent)
                .thenRun{ englishBot.sendItemsList(userData.chatId, userData.dictionaries.map{it.name}, true) }
        } else {
            behavior.Effect().none().thenNoReply()
        }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when (clazz) {
            ShowDictionariesEvent::class.java -> ShowDictionariesState(userData)
            else -> this
        }
    }

    override fun runOnBack(englishBot: EnglishBot) {}

    override fun backState(): State {
        return this
    }
}