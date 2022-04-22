package com.blackmorse.telegrambotenglish.akka

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import com.blackmorse.telegrambotenglish.EnglishBot

sealed class SupervisorMessage

data class SimpleText(val text: String, val chatId: String) : SupervisorMessage()

class BotSupervisor(private val englishBot: EnglishBot, context: ActorContext<SupervisorMessage>?) : AbstractBehavior<SupervisorMessage>(context) {
    init {
        context?.log?.info("Actor System for English Bot has started")
    }
    override fun createReceive(): Receive<SupervisorMessage> {
        return newReceiveBuilder()
            .onMessage(SimpleText::class.java) {msg ->
                englishBot.justSendText(msg.text, msg.chatId)
                return@onMessage this
            }.build()
    }

    companion object {
        fun createBehavior(englishBot: EnglishBot): Behavior<SupervisorMessage> {
            return Behaviors.setup<SupervisorMessage>{context -> BotSupervisor(englishBot, context)}
        }
    }
}