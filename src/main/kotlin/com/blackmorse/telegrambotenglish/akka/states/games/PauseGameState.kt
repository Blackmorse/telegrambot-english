package com.blackmorse.telegrambotenglish.akka.states.games

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.Commands
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.blackmorse.telegrambotenglish.akka.states.ShowDictionariesState
import com.blackmorse.telegrambotenglish.akka.states.ShowDictionaryState
import com.blackmorse.telegrambotenglish.akka.states.State

object YesEvent: Event
object NoEvent: Event

enum class PauseType {
    BACK, MAIN_MENU
}

class PauseGameState<T: GameData>(private val pauseType: PauseType,
                                  userData: UserData,
                                  dictionary: Dictionary,
                                  private val gameState: GameState<T>,
                                  chainGamesData: List<GameData>): GameState<T>(userData, dictionary, gameState.gameData, chainGamesData) {
    override fun doHandleMessage(msg: TelegramMessage,
                                 englishBot: EnglishBot,
                                 behavior: EventSourcedBehavior<TelegramMessage, Event, State>): Effect<Event, State> {
        return if (msg.update.message.text == Commands.YES.text) {
            behavior.Effect().persist(YesEvent)
                    .thenRun { state: State -> state.sendBeforeStateMessage(englishBot) }
        } else {
            behavior.Effect().persist(NoEvent)
                    .thenRun { state: GameState<T> -> state.sendBeforeStateMessage(englishBot) }
        }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when (clazz) {
            YesEvent::class.java ->
                if(pauseType == PauseType.BACK)
                    ShowDictionaryState(userData, dictionary)
                else
                    ShowDictionariesState(userData)
            else -> this.gameState
        }
    }

    override fun sendBeforeStateMessage(englishBot: EnglishBot) {
        englishBot.sendConfirmation(userData.chatId)
    }

    override fun backState(): State {
        return this.gameState
    }
}