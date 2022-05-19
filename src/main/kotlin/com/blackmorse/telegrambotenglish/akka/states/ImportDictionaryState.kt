package com.blackmorse.telegrambotenglish.akka.states

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.WordWithTranslation
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.fasterxml.jackson.annotation.JsonProperty

data class DictionaryImportedEvent(
    @JsonProperty("dictionary")
    val dictionary: Dictionary
) : Event

object IncorrectDictionaryFormatEvent : Event

class ImportDictionaryState(
    @JsonProperty("userData")
    userData: UserData) : State(userData) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<TelegramMessage, Event, State>
    ): Effect<Event, State> {
        fun wrongFormatResponse(): Effect<Event, State> {
            return behavior.Effect().persist(IncorrectDictionaryFormatEvent)
                .thenRun{ state: State ->
                    englishBot.justSendText("Incorrect dictionary format!", userData.chatId)
                    state.sendBeforeStateMessage(englishBot)
                }
        }

        val message = msg.update.message.text
        val dictSplit = message.split("\n\n")
        if (dictSplit.size != 2) {
            return wrongFormatResponse()
        }
        val dictionaryName = dictSplit[0].trim()
        val words = dictSplit[1].split("\n")
        val wordsWithTranslation = mutableListOf<WordWithTranslation>()

        for (wordLine in words) {
            val wordLineSplit = wordLine.split(".", limit = 2)
            if(wordLineSplit.size < 2) return wrongFormatResponse()
            val word = wordLineSplit[1].trim()

            val wordWithTranslationSplit = word.split("<->", limit = 2)
            if (wordWithTranslationSplit.size < 2) {
                return wrongFormatResponse()
            }
            wordsWithTranslation.add(WordWithTranslation(wordWithTranslationSplit[0].trim(), wordWithTranslationSplit[1].trim()))
        }
        val dictionary = Dictionary(dictionaryName, wordsWithTranslation.toList())
        return behavior.Effect().persist(DictionaryImportedEvent(dictionary))
            .thenRun { state: State -> state.sendBeforeStateMessage(englishBot) }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when(clazz) {
            IncorrectDictionaryFormatEvent::class.java -> this
            DictionaryImportedEvent::class.java -> {
                val dictionary = (event as DictionaryImportedEvent).dictionary
                ShowDictionariesState(userData.copy(dictionaries = userData.dictionaries + dictionary))
            }
            else -> this
        }
    }

    override fun sendBeforeStateMessage(englishBot: EnglishBot) {
        englishBot.justSendText("Enter dictionary content:", userData.chatId)
    }

    override fun backState(): State {
        return ShowDictionariesState(userData)
    }
}