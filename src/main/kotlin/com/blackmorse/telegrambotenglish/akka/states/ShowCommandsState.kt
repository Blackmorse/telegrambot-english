package com.blackmorse.telegrambotenglish.akka.states

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.ShowDictionariesEvent
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.Commands
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.fasterxml.jackson.annotation.JsonProperty

class ShowCommandsState(
    @JsonProperty("userData")
    userData: UserData) : State(userData) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<TelegramMessage, Event, State>
    ): Effect<Event, State> {
        return if (msg.update.message.text == Commands.SHOW_DICTIONARIES.text) {
            behavior.Effect().persist(ShowDictionariesEvent)
                .thenRun{ state: ShowDictionariesState -> state.sendBeforeStateMessage(englishBot) }
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

    override fun backState(): State {
        return this
    }

    override fun sendBeforeStateMessage(englishBot: EnglishBot) {
        englishBot.sendCommandsList(userData.chatId)
    }
}
