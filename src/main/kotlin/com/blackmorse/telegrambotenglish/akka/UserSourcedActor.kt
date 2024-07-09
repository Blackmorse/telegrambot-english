package com.blackmorse.telegrambotenglish.akka

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.javadsl.*
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.blackmorse.telegrambotenglish.akka.messages.UserActorMessage
import com.blackmorse.telegrambotenglish.akka.messages.WordOfTheDay
import com.blackmorse.telegrambotenglish.akka.states.*
import com.blackmorse.telegrambotenglish.akka.states.games.PauseGameState
import com.blackmorse.telegrambotenglish.akka.states.games.combineletters.CombineLettersGameState
import com.blackmorse.telegrambotenglish.akka.states.games.fourchoices.FourChoicesGameState
import com.blackmorse.telegrambotenglish.akka.states.games.twocolumns.TwoColumnsGameLeftColumnSelectedState
import com.blackmorse.telegrambotenglish.akka.states.games.twocolumns.TwoColumnsGameState
import com.blackmorse.telegrambotenglish.akka.states.games.typetranslation.TypeTranslationGameState

interface Event

object AddDictionaryEvent : Event
object DeleteDictionaryEvent : Event

class UserSourcedActor(val chatId: String, val englishBot: EnglishBot, private val classes: List<Class<out State>>) :
        EventSourcedBehavior<UserActorMessage, Event, State>(PersistenceId.ofUniqueId("userActor$chatId")) {
    override fun commandHandler(): CommandHandler<UserActorMessage, Event, State> {
        val builder = CommandHandlerBuilder.builder<UserActorMessage, Event, State>()

        classes.forEach{clazz ->
            builder.forStateType(clazz)
                .onCommand(TelegramMessage::class.java){state, msg -> state.handleMessage(msg, englishBot, this)}
                .onCommand(WordOfTheDay::class.java){state, _ -> state.handleWordOfTheDay(englishBot, this)}
        }

            builder
            .forAnyState()
            .onAnyCommand{_ -> Effect().none().thenRun { println("Dead letters") }}
        return builder.build()

    }

    override fun eventHandler(): EventHandler<State, Event> {
        val builder = EventHandlerBuilder.builder<State, Event>()

        classes.forEach { clazz ->
            builder.forStateType(clazz)
                .onAnyEvent { state, event -> state.handleEvent(event.javaClass, state, event) }
        }

        return builder.build()
    }

    override fun emptyState(): State {
        return HelloScreenState(UserData(chatId, emptyList()))
    }

    override fun retentionCriteria(): RetentionCriteria {
        return RetentionCriteria.snapshotEvery(3, 3).withDeleteEventsOnSnapshot()
    }

    companion object {
        fun create (chatId: String, englishBot: EnglishBot): Behavior<UserActorMessage> {
            return UserSourcedActor(chatId, englishBot,
            listOf(
                HelloScreenState::class.java,
                ShowCommandsState::class.java,
                ShowDictionariesState::class.java,
                AddDictionaryState::class.java,
                DeleteDictionaryState::class.java,
                ShowDictionaryState::class.java,
                AddWordToDictionaryState::class.java,
                AddTranslationToWordState::class.java,
                DeleteWordFromDictionaryState::class.java,
                TwoColumnsGameState::class.java,
                TwoColumnsGameLeftColumnSelectedState::class.java,
                TypeTranslationGameState::class.java,
                FourChoicesGameState::class.java,
                CombineLettersGameState::class.java,
                ImportDictionaryState::class.java,
                PauseGameState::class.java,
                WordOfDayState::class.java
            ))
        }
    }
}