package com.blackmorse.telegrambotenglish.akka.states

import akka.persistence.typed.javadsl.Effect
import akka.persistence.typed.javadsl.EventSourcedBehavior
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.Event
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.WordWithTranslation
import com.blackmorse.telegrambotenglish.akka.messages.Commands
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.blackmorse.telegrambotenglish.akka.messages.UserActorMessage
import com.blackmorse.telegrambotenglish.akka.states.games.NoEvent
import com.blackmorse.telegrambotenglish.akka.states.games.YesEvent
import com.blackmorse.telegrambotenglish.akka.states.games.combineletters.CombineLettersGameData
import com.blackmorse.telegrambotenglish.akka.states.games.typetranslation.TypeTranslationGameData
import java.util.Optional
import kotlin.random.Random

class WordOfDayState(userData: UserData,
                     private val sourceState: State,
                     private val wordWithTranslation: Optional<WordWithTranslation>,
                     val seed: Long) : State(userData) {
    override fun doHandleMessage(msg: TelegramMessage, englishBot: EnglishBot, behavior: EventSourcedBehavior<UserActorMessage, Event, State>): Effect<Event, State> {
        return if (msg.update.message.text == Commands.YES.text) {
            behavior.Effect().persist(YesEvent)
                    .thenRun { state: State -> state.sendBeforeStateMessage(englishBot) }
        } else {
            behavior.Effect().persist(NoEvent)
                    .thenRun { state: State -> state.sendBeforeStateMessage(englishBot) }
        }
    }

    override fun doHandleEvent(clazz: Any, state: State, event: Event): State {
        return when(clazz) {
            YesEvent::class.java -> {
                val random = Random(seed)
                val word = wordWithTranslation.get()
                val gameDatas = listOf(
                            TypeTranslationGameData.init(word),
                            TypeTranslationGameData.reverseInit(word),
                            CombineLettersGameData.init(word, random),
                            CombineLettersGameData.reverseInit(word, random)
                    ).shuffled(random)

                gameDatas[0].createState(userData, gameDatas - gameDatas[0], sourceState)
            }
            else -> sourceState
        }
    }

    override fun handleWordOfTheDay(englishBot: EnglishBot, behavior: EventSourcedBehavior<UserActorMessage, Event, State>): Effect<Event, State> {
        return behavior.Effect().none()
    }

    override fun sendBeforeStateMessage(englishBot: EnglishBot) {
        if (wordWithTranslation.isPresent) {
            englishBot.sendConfirmation(userData.chatId, "The word of the day: ${wordWithTranslation.get()}. Continue?")
        } else {
            englishBot.justSendText("No words to learn yet", userData.chatId)
        }
    }

    override fun backState(): State {
        return sourceState
    }
}