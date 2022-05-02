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
object DeleteWordEvent : Event

class ShowDictionaryState(userData: UserData, val dictionary: Dictionary) : State(userData) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<TelegramMessage, Event, State>
    ): Effect<Event, State> {
        return if (msg.update.message.text == Commands.ADD_WORD.text) {
            behavior.Effect().persist(AddWordEvent)
                .thenRun{ state: AddWordToDictionaryState -> state.sendBeforeStateMessage(englishBot) }
        } else if(msg.update.message.text == Commands.DELETE_WORD.text) {
            behavior.Effect().persist(DeleteWordEvent)
                .thenRun { state: DeleteWordFromDictionaryState -> state.sendBeforeStateMessage(englishBot) }
        } else {
            behavior.Effect().none().thenNoReply()
        }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when (clazz) {
            AddWordEvent::class.java -> AddWordToDictionaryState(userData, dictionary)
            DeleteWordEvent::class.java -> DeleteWordFromDictionaryState(userData, dictionary)
            else -> this
        }
    }

    override fun sendBeforeStateMessage(englishBot: EnglishBot) {
        englishBot.sendDictionaryInfo(userData.chatId, dictionary)
    }

    override fun backState(): State {
        return ShowDictionariesState(userData)
    }
}
