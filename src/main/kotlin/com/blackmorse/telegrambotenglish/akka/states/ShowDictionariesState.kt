package com.blackmorse.telegrambotenglish.akka.states

import akka.persistence.typed.javadsl.*
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.*
import com.blackmorse.telegrambotenglish.akka.messages.Commands
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage

class ShowDictionariesState(userData: UserData) : State(userData) {
    override fun doHandleMessage(msg: TelegramMessage,
                      englishBot: EnglishBot,
                      behavior: EventSourcedBehavior<TelegramMessage, Event, State>): Effect<Event,  State> {
        return when (msg.update.message.text) {
            Commands.ADD_DICTIONARY.text -> {
                behavior.Effect().persist(AddDictionaryEvent)
                    .thenRun { englishBot.justSendText("Enter dictionary name:", userData.chatId) }
            }
            Commands.DELETE_DICTIONARY.text -> {
                behavior.Effect().persist(DeleteDictionaryEvent)
                    .thenRun{ englishBot.sendDictionariesList(userData.chatId, userData.dictionaries, false)}
            }
            else -> {
                behavior.Effect().none().thenNoReply()
            }
        }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when (clazz) {
            AddDictionaryEvent::class.java -> AddDictionaryState(userData)
            DeleteDictionaryEvent::class.java -> DeleteDictionaryState(userData)
            else -> this
        }
    }

    override fun runOnBack(englishBot: EnglishBot) {
        englishBot.sendCommandsList(userData.chatId)
    }

    override fun backState(): State {
        return ShowCommandsState(userData)
    }
}