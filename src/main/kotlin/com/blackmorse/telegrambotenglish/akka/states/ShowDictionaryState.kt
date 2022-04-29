package com.blackmorse.telegrambotenglish.akka.states

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.Commands
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage

object AddWordEvent : Event

class ShowDictionaryState(userData: UserData, val dictionary: Dictionary) : State(userData) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<TelegramMessage, Event, State>
    ): Effect<Event, State> {
        return if (msg.update.message.text == Commands.ADD_WORD.text) {
            behavior.Effect().persist(AddWordEvent)
                .thenRun{ englishBot.justSendText("Please enter the word: ", userData.chatId) }
        } else {
            behavior.Effect().none().thenNoReply()
        }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when (clazz) {
            AddWordEvent::class.java -> AddWordToDictionaryState(userData, dictionary)
            else -> this
        }
    }

    override fun runOnBack(englishBot: EnglishBot) {
        englishBot.sendDictionariesList(userData.chatId, userData.dictionaries.map { it.name }, true)
    }

    override fun backState(): State {
        return ShowDictionariesState(userData)
    }
}