package com.blackmorse.telegrambotenglish.akka

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.javadsl.*
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.blackmorse.telegrambotenglish.akka.states.*

interface Event
object ShowCommandsEvent: Event
object ShowDictionariesEvent: Event
object AddDictionaryEvent : Event
object DeleteDictionaryEvent : Event

class EventSourcedUserActor(val chatId: String, val englishBot: EnglishBot) :
        EventSourcedBehavior<TelegramMessage, Event, State>(PersistenceId.ofUniqueId("userActor$chatId")) {
    override fun commandHandler(): CommandHandler<TelegramMessage, Event, State> {
        val builder = CommandHandlerBuilder.builder<TelegramMessage, Event, State>()

        builder.forStateType(HelloScreenState::class.java)
            .onCommand(TelegramMessage::class.java){state, msg -> state.handleMessage(msg, englishBot, this)}

        builder.forStateType(ShowCommandsState::class.java)
            .onCommand(TelegramMessage::class.java){state, msg -> state.handleMessage(msg, englishBot, this)}

        builder.forStateType(ShowDictionariesState::class.java)
            .onCommand(TelegramMessage::class.java){state, msg -> state.handleMessage(msg, englishBot, this)}

        builder.forStateType(AddDictionaryState::class.java)
            .onCommand(TelegramMessage::class.java){state, msg -> state.handleMessage(msg, englishBot, this)}

        builder.forStateType(DeleteDictionaryState::class.java)
            .onCommand(TelegramMessage::class.java){state, msg -> state.handleMessage(msg, englishBot, this)}


        builder.forStateType(ShowDictionaryState::class.java)
            .onCommand(TelegramMessage::class.java){state, msg -> state.handleMessage(msg, englishBot, this)}

        builder.forStateType(AddWordToDictionaryState::class.java)
            .onCommand(TelegramMessage::class.java){state, msg -> state.handleMessage(msg, englishBot, this)}

        builder.forStateType(AddTranslationToWordState::class.java)
            .onCommand(TelegramMessage::class.java){state, msg -> state.handleMessage(msg, englishBot, this)}

        builder
            .forAnyState()
            .onAnyCommand{_ -> Effect().none().thenRun { println("Dead letters") }}
        return builder.build()
    }

    override fun eventHandler(): EventHandler<State, Event> {
        val builder = EventHandlerBuilder.builder<State, Event>()

        builder.forStateType(HelloScreenState::class.java)
            .onAnyEvent{state, event -> state.handleEvent(event.javaClass, state, event)}

        builder.forStateType(ShowCommandsState::class.java)
            .onAnyEvent{state, event -> state.handleEvent(event.javaClass, state, event)}

        builder.forStateType(ShowDictionariesState::class.java)
            .onAnyEvent{state, event -> state.handleEvent(event.javaClass, state, event)}

        builder.forStateType(AddDictionaryState::class.java)
            .onAnyEvent{state, event -> state.handleEvent(event.javaClass, state, event)}

        builder.forStateType(DeleteDictionaryState::class.java)
            .onAnyEvent{state, event -> state.handleEvent(event.javaClass, state, event)}

        builder.forStateType(ShowDictionaryState::class.java)
            .onAnyEvent{state, event -> state.handleEvent(event.javaClass, state, event)}

        builder.forStateType(AddWordToDictionaryState::class.java)
            .onAnyEvent{state, event -> state.handleEvent(event.javaClass, state, event)}

        builder.forStateType(AddTranslationToWordState::class.java)
            .onAnyEvent{state, event -> state.handleEvent(event.javaClass, state, event)}

        return builder.build()
    }

    override fun emptyState(): State {
        return HelloScreenState(UserData(chatId, emptyList()))
    }

    companion object {
        fun create (chatId: String, englishBot: EnglishBot): Behavior<TelegramMessage> {
            return EventSourcedUserActor(chatId, englishBot)
        }
    }
}