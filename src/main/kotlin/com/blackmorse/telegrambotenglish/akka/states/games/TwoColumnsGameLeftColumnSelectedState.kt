package com.blackmorse.telegrambotenglish.akka.states.games

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.WordWithTranslation
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.blackmorse.telegrambotenglish.akka.states.ShowDictionaryState
import com.blackmorse.telegrambotenglish.akka.states.State
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class WordGuessedEvent(
    @JsonProperty("wordWithTranslation")
    val wordWithTranslation: WordWithTranslation) : Event

object WordNotGuessedEvent : Event

class TwoColumnsGameLeftColumnSelectedState(userData: UserData, val dictionary: Dictionary, val gameData: TwoColumnsGameData) : State(userData) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<TelegramMessage, Event, State>
    ): Effect<Event, State> {
        val selectedWord = msg.update.message.text
        val wordWithTranslationOpt = gameData.checkRightWord(selectedWord)
        return if (wordWithTranslationOpt.isPresent) {
            behavior.Effect().persist(WordGuessedEvent(wordWithTranslationOpt.get()))
                .thenRun { state: State -> state.sendBeforeStateMessage(englishBot) }
        } else {
            behavior.Effect().persist(WordNotGuessedEvent)
                .thenRun { state: State ->
                    englishBot.justSendText("Wrong match!", userData.chatId)
                    state.sendBeforeStateMessage(englishBot)
                }
        }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when (clazz) {
            WordGuessedEvent::class.java -> {
                if (gameData.words.size == 1) {
                    ShowDictionaryState(userData, dictionary)
                } else {
                    val wgEvent = event as WordGuessedEvent
                    val newWords = gameData.words - wgEvent.wordWithTranslation
                    val newLeftColumn = gameData.leftColumn - wgEvent.wordWithTranslation.word
                    val newRightColumn = gameData.rightColumn - wgEvent.wordWithTranslation.translation

                    val newGameData = TwoColumnsGameData(newWords, newLeftColumn, newRightColumn, Optional.empty())
                    TwoColumnsGameState(userData, dictionary, newGameData)
                }
            }
            WordNotGuessedEvent -> this
            else -> this
        }
    }

    override fun sendBeforeStateMessage(englishBot: EnglishBot) {
        englishBot.sendTwoColumnsGame(userData.chatId, gameData)
    }

    override fun backState(): State {
        return ShowDictionaryState(userData, dictionary)
    }

}
