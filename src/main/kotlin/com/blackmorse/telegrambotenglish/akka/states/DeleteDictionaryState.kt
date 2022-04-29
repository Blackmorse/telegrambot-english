package com.blackmorse.telegrambotenglish.akka.states

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.fasterxml.jackson.annotation.JsonProperty

data class DictionaryDeletedEvent(
    @JsonProperty("dictionaryName")
    val dictionaryName: String
    ) : Event

class DeleteDictionaryState(userData: UserData) : State(userData) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<TelegramMessage, Event, State>
    ): Effect<Event, State> {
        val dictNameSplit = msg.update.message.text.split(". ", limit = 2)

        return if (dictNameSplit.size == 2 && userData.dictionaries.contains(dictNameSplit[1])) {
            val dictName = dictNameSplit[1]

            behavior.Effect().persist(DictionaryDeletedEvent(dictName))
                .thenRun{ state : ShowDictionariesState -> englishBot.sendDictionariesList(userData.chatId, state.userData.dictionaries, true)}
        } else {
            behavior.Effect().none().thenRun{
                englishBot.justSendText("There is no dictionary ${msg.update.message.text}", userData.chatId)
                englishBot.sendDictionariesList(userData.chatId, userData.dictionaries, false)
            }
        }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when (clazz) {
            DictionaryDeletedEvent::class.java -> {
                val dictionaryName = (event as DictionaryDeletedEvent).dictionaryName
                val newUserData = userData.copy(dictionaries = userData.dictionaries - dictionaryName)
                ShowDictionariesState(newUserData)
            }
            else -> this
        }
    }

    override fun runOnBack(englishBot: EnglishBot) {
        englishBot.sendDictionariesList(userData.chatId, userData.dictionaries, true)
    }

    override fun backState(): State {
        return ShowDictionariesState(userData)
    }
}
