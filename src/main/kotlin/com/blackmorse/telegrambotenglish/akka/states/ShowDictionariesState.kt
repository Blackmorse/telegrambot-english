package com.blackmorse.telegrambotenglish.akka.states

import akka.persistence.typed.javadsl.*
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.*
import com.blackmorse.telegrambotenglish.akka.messages.Commands
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.fasterxml.jackson.annotation.JsonProperty

data class SelectDictionaryEvent(
    @JsonProperty("dictionaryName")
    val dictionaryName: String
    ) : Event

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
                    .thenRun{ englishBot.sendItemsList(userData.chatId, userData.dictionaries.map{it.name}, false)}
            }
            else -> {
                val dictionaryNameOpt = Dictionary.getItemFromIndexedList(msg.update.message.text)
                if (dictionaryNameOpt.isPresent && userData.dictionaries.map{ it.name }.contains(dictionaryNameOpt.get())) {
                    val dictionary = userData.dictionaries.find { it.name == dictionaryNameOpt.get() }!!
                    behavior.Effect().persist(SelectDictionaryEvent(dictionaryNameOpt.get()))
                        .thenRun{ englishBot.sendDictionaryInfo(userData.chatId, dictionary) }
                } else {
                    behavior.Effect().none().thenNoReply()
                }
            }
        }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when (clazz) {
            AddDictionaryEvent::class.java -> AddDictionaryState(userData)
            DeleteDictionaryEvent::class.java -> DeleteDictionaryState(userData)
            SelectDictionaryEvent::class.java -> ShowDictionaryState(userData, userData.dictionaries.find { it.name == (event as SelectDictionaryEvent).dictionaryName }!!)
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