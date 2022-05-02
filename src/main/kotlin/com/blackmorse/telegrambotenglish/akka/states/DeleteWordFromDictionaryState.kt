package com.blackmorse.telegrambotenglish.akka.states

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.fasterxml.jackson.annotation.JsonProperty

data class WordDeletedEvent(
    @JsonProperty("word")
    val word: String
): Event

class DeleteWordFromDictionaryState(userData: UserData, val dictionary: Dictionary) : State(userData) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<TelegramMessage, Event, State>
    ): Effect<Event, State> {
        val itemOpt = Dictionary.getItemFromIndexedList(msg.update.message.text)
        return if (itemOpt.isPresent && dictionary.words.find { it.toString() == itemOpt.get() } != null) {
            behavior.Effect().persist(WordDeletedEvent(itemOpt.get()))
                .thenRun{ state: ShowDictionaryState -> state.sendBeforeStateMessage(englishBot) }
        } else {
            behavior.Effect().none()
                .thenRun{
                    englishBot.justSendText("There is now word ${msg.update.message.text}", userData.chatId)
                    sendBeforeStateMessage(englishBot)
                }
        }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when (clazz) {
            WordDeletedEvent::class.java -> {
                val wordToDelete = dictionary.words.find { it.toString() == (event as WordDeletedEvent).word }!!
                val newWords = dictionary.words - wordToDelete
                val newDictionary = dictionary.copy(words = newWords)
                userData.dictionaries.find { it.name == dictionary.name }
                val newUserData = userData.copy(dictionaries = userData.dictionaries - dictionary + newDictionary)
                ShowDictionaryState(newUserData, newDictionary)
            }
            else -> this
        }
    }

    override fun backState(): State {
        return ShowDictionaryState(userData, dictionary)
    }

    override fun sendBeforeStateMessage(englishBot: EnglishBot) {
        englishBot.sendItemsList(userData.chatId, dictionary.words.map { it.toString() }, false)
    }
}
