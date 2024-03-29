package com.blackmorse.telegrambotenglish.akka.states.games.typetranslation

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.blackmorse.telegrambotenglish.akka.messages.UserActorMessage
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
                               gameData: TypeTranslationGameData,
                               chainGamesData: List<GameData>,
                               stateAfterFinish: State) : GameState<TypeTranslationGameData>(userData, gameData, chainGamesData, stateAfterFinish) {
    override fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<UserActorMessage, Event, State>
    ): Effect<Event, State> {
        val attempt = msg.update.message.text
        return if (checkResult(attempt, gameData.word.translation)) {
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

    private fun checkResult(attempt: String, result: String): Boolean {
        val uppercaseAttempt = attempt.uppercase()
        if (uppercaseAttempt == gameData.word.translation.uppercase()) {
            return true
        }
        return result.split(",").map { it.trim().uppercase() }
                .any { it == uppercaseAttempt }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when (clazz) {
            WrongTranslationTypedEvent::class.java -> this
            CorrectTranslationTypedEvent::class.java -> finishGame()
            else -> this
        }
    }

    override fun sendBeforeStateMessage(englishBot: EnglishBot) {
        englishBot.justSendText("Type in translation of word \"${gameData.word.word}\"", userData.chatId)
    }
}