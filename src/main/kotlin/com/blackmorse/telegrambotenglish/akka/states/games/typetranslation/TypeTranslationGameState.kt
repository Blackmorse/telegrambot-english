package com.blackmorse.telegrambotenglish.akka.states.games.typetranslation

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.blackmorse.telegrambotenglish.akka.states.ShowDictionaryState
import com.blackmorse.telegrambotenglish.akka.states.State
import com.blackmorse.telegrambotenglish.akka.states.games.GameData
import com.blackmorse.telegrambotenglish.akka.states.games.GameState
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class CorrectTranslationTypedEvent(
    @JsonProperty("translation")
    val translation: String
) : Event

object WrongTranslationTypedEvent : Event

class TypeTranslationGameState(userData: UserData,
                               dictionary: Dictionary,
                               gameData: TypeTranslationGameData,
                               chainGamesData: List<GameData>) : GameState<TypeTranslationGameData>(userData, dictionary, gameData, chainGamesData) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<TelegramMessage, Event, State>
    ): Effect<Event, State> {
        val attempt = msg.update.message.text
        return if (attempt.uppercase() == gameData.word.translation.uppercase()) {
            behavior.Effect().persist(CorrectTranslationTypedEvent(attempt))
                .thenRun { state: State -> state.sendBeforeStateMessage(englishBot) }
        } else {
            behavior.Effect().persist(WrongTranslationTypedEvent)
                .thenRun { state: TypeTranslationGameState ->
                    englishBot.justSendText("Wrong answer!", userData.chatId)
                    state.sendBeforeStateMessage(englishBot)
                }
        }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when (clazz) {
            WrongTranslationTypedEvent::class.java -> this
            CorrectTranslationTypedEvent::class.java -> {
                if (chainGamesData.isEmpty()) {
                    ShowDictionaryState(userData, dictionary)
                } else {
                    chainGamesData[0].createState(userData, dictionary, chainGamesData - chainGamesData[0])
                }
            }
            else -> this
        }
    }

    override fun sendBeforeStateMessage(englishBot: EnglishBot) {
        englishBot.justSendText("Type in translation of word \"${gameData.word.word}\"", userData.chatId)
    }

    override fun backState(): State {
        return ShowDictionaryState(userData, dictionary)
    }
}