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

data class TranslationToWordEnteredEvent(
    @JsonProperty("translation")
    val translation: String
    ) : Event

class AddTranslationToWordState(
    @JsonProperty("userData")
    userData: UserData,
    @JsonProperty("dictionary")
    val dictionary: Dictionary,
    @JsonProperty("word")
    val word: String) : State(userData) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<TelegramMessage, Event, State>
    ): Effect<Event, State> {
        return behavior.Effect().persist(TranslationToWordEnteredEvent(msg.update.message.text))
            .thenRun { state: ShowDictionaryState -> state.sendBeforeStateMessage(englishBot) }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        userData.dictionaries.find { it === dictionary }!!
        val newWord = WordWithTranslation(word, (event as TranslationToWordEnteredEvent).translation)
        val newDictionary = dictionary.copy(words = dictionary.words + listOf(newWord))
        val newUserData = userData.copy(dictionaries = userData.dictionaries - dictionary + listOf(newDictionary))
        return ShowDictionaryState(newUserData, newDictionary)
    }

    override fun backState(): State {
        return AddWordToDictionaryState(userData, dictionary)
    }

    override fun sendBeforeStateMessage(englishBot: EnglishBot) {
        englishBot.justSendText("Enter the translation for $word:", userData.chatId)   
    }
}
