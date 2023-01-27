package com.blackmorse.telegrambotenglish.akka.states

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.Commands
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.blackmorse.telegrambotenglish.akka.messages.UserActorMessage
import com.blackmorse.telegrambotenglish.akka.messages.WordOfTheDay
import java.util.*
import kotlin.random.Random

object BackEvent : Event
object MainMenuEvent : Event

abstract class State(val userData: UserData) {
    fun handleMessage(msg: TelegramMessage,
                      englishBot: EnglishBot,
                      behavior: EventSourcedBehavior<UserActorMessage, Event, State>
    ): Effect<Event, State> {
        return when (msg.update.message.text) {
            Commands.BACK.text -> {
                behavior.Effect().persist(BackEvent)
                    .thenRun{ state: State -> state.sendBeforeStateMessage(englishBot) }
            }
            Commands.MAIN_MENU.text -> {
                behavior.Effect().persist(MainMenuEvent)
                    .thenRun{ state: State -> state.sendBeforeStateMessage(englishBot)}
            }
            else -> {
                doHandleMessage(msg, englishBot, behavior)
            }
        }
    }

    open fun handleWordOfTheDay(englishBot: EnglishBot, behavior: EventSourcedBehavior<UserActorMessage, Event, State>): Effect<Event, State> {
        return behavior.Effect().persist(WordOfTheDayEvent(System.nanoTime()))
                .thenRun { state: State -> state.sendBeforeStateMessage(englishBot) }
    }

    fun wordOfTheDay(previousState: State, seed: Long): WordOfDayState {
        val allWords = userData.dictionaries.flatMap { it.words }
        val word = if (allWords.isEmpty()) {
            Optional.empty()
        } else {
            val random = Random(seed)
            val wordNumber = random.nextInt(allWords.size)
            Optional.of(allWords[wordNumber])
        }

        return WordOfDayState(userData, previousState, word, seed)
    }

    fun handleEvent(clazz: Any, state: State, event: Event): State {
        return when (clazz) {
            BackEvent::class.java -> backState()
            MainMenuEvent::class.java -> mainMenuState(state)
            WordOfTheDayEvent::class.java -> wordOfTheDay(this, (event as WordOfTheDayEvent).seed)
            else -> doHandleEvent(clazz, state, event)
        }
    }

    protected abstract fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<UserActorMessage, Event, State>
    ): Effect<Event, State>

    protected abstract fun doHandleEvent(clazz: Any, state: State, event: Event): State

    abstract fun sendBeforeStateMessage(englishBot: EnglishBot)

    abstract fun backState(): State

    protected open fun mainMenuState(state: State): State {
        return ShowCommandsState(state.userData)
    }
}
