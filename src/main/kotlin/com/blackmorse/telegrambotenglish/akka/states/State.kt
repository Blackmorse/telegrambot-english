package com.blackmorse.telegrambotenglish.akka.states

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.Commands
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage

object BackEvent : Event
object MainMenuEvent : Event

abstract class State(val userData: UserData) {
    fun handleMessage(msg: TelegramMessage,
                      englishBot: EnglishBot,
                      behavior: EventSourcedBehavior<TelegramMessage, Event, State>
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

    fun handleEvent(clazz: Any, state: State, event: Event): State {
        return when (clazz) {
            BackEvent::class.java -> backState()
            MainMenuEvent::class.java -> mainMenuState(state)
            else -> doHandleEvent(clazz, state, event)
        }
    }

    protected abstract fun doHandleMessage(
        msg: TelegramMessage,
        englishBot: EnglishBot,
        behavior: EventSourcedBehavior<TelegramMessage, Event, State>
    ): Effect<Event, State>

    protected abstract fun doHandleEvent(clazz: Any, state: State, event: Event): State

    abstract fun sendBeforeStateMessage(englishBot: EnglishBot)

    abstract fun backState(): State

    protected open fun mainMenuState(state: State): State {
        return ShowCommandsState(state.userData)
    }
}
