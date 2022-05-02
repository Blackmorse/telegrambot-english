package com.blackmorse.telegrambotenglish.akka.states

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.fasterxml.jackson.annotation.JsonProperty

data class WordEnteredEvent(
    @JsonProperty("word")
    val word: String
    ) : Event

class AddWordToDictionaryState(userData: UserData, private val dictionary: Dictionary) : State(userData) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<TelegramMessage, Event, State>
    ): Effect<Event, State> {
        return behavior.Effect().persist(WordEnteredEvent(msg.update.message.text))
            .thenRun { state: AddTranslationToWordState -> state.sendBeforeStateMessage(englishBot) }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return AddTranslationToWordState(userData, dictionary, (event as WordEnteredEvent).word)
    }

    override fun backState(): State {
        return ShowDictionaryState(userData, dictionary)
    }

    override fun sendBeforeStateMessage(englishBot: EnglishBot) {
        englishBot.justSendText("Please enter the word: ", userData.chatId)
    }
}
