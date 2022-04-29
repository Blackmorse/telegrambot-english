package com.blackmorse.telegrambotenglish.akka.states

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.fasterxml.jackson.annotation.JsonProperty

data class DictionaryAddedEvent(
    @JsonProperty("dictionaryName")
    val dictionaryName: String
    ) : Event

class AddDictionaryState(userData: UserData) : State(userData) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<TelegramMessage, Event, State>
    ): Effect<Event, State> {
        val dictionaryName = msg.update.message.text
        return behavior.Effect().persist(DictionaryAddedEvent(dictionaryName))
            .thenRun{ state: ShowDictionariesState ->
                englishBot.sendDictionariesList(state.userData.chatId, state.userData.dictionaries.map{it.name}, true)
            }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when(clazz) {
            DictionaryAddedEvent::class.java -> {
                val dictionaryName = (event as DictionaryAddedEvent).dictionaryName
                val dictionary = Dictionary(name = dictionaryName, words = emptyList())
                ShowDictionariesState(userData.copy(dictionaries = userData.dictionaries + listOf(dictionary)))
            }
            else -> this
        }
    }

    override fun runOnBack(englishBot: EnglishBot) {
        englishBot.sendDictionariesList(userData.chatId, userData.dictionaries.map{it.name}, true)
    }

    override fun backState(): State {
        return ShowDictionariesState(userData)
    }

}
