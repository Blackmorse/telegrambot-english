package com.blackmorse.telegrambotenglish.akka.states

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.fasterxml.jackson.annotation.JsonProperty

data class DictionaryDeletedEvent(
    @JsonProperty("dictionaryName")
    val dictionaryName: String
    ) : Event

class DeleteDictionaryState(
    @JsonProperty("userData")
    userData: UserData) : State(userData) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<TelegramMessage, Event, State>
    ): Effect<Event, State> {
        val dictNameOpt = Dictionary.getItemFromIndexedList(msg.update.message.text)

        return if (dictNameOpt.isPresent && userData.dictionaries.map{it.name}.contains(dictNameOpt.get())) {
            val dictName = dictNameOpt.get()

            behavior.Effect().persist(DictionaryDeletedEvent(dictName))
                .thenRun{ state : ShowDictionariesState -> state.sendBeforeStateMessage(englishBot) }
        } else {
            behavior.Effect().none().thenRun{
                englishBot.justSendText("There is no dictionary ${msg.update.message.text}", userData.chatId)
                sendBeforeStateMessage(englishBot)
            }
        }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when (clazz) {
            DictionaryDeletedEvent::class.java -> {
                val dictionaryName = (event as DictionaryDeletedEvent).dictionaryName
                val dictionary = userData.dictionaries.find { it.name == dictionaryName }
                if (dictionary != null) {
                    val newUserData = userData.copy(dictionaries = userData.dictionaries - dictionary)
                    ShowDictionariesState(newUserData)
                } else {
                    ShowDictionariesState(userData)
                }
            }
            else -> this
        }
    }

    override fun sendBeforeStateMessage(englishBot: EnglishBot) {
        englishBot.sendItemsList(userData.chatId, userData.dictionaries.map{it.name}, false)
    }

    override fun backState(): State {
        return ShowDictionariesState(userData)
    }
}
