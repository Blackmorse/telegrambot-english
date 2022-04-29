package com.blackmorse.telegrambotenglish.akka

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import org.telegram.telegrambots.meta.api.objects.Update


class BotSupervisor(private val englishBot: EnglishBot,
                    context: ActorContext<Update>?) : AbstractBehavior<Update>(context) {
    init {
        context?.log?.info("Actor System for English Bot has started")
    }

    private val userActorsMap = HashMap<String, ActorRef<TelegramMessage>>()
    override fun createReceive(): Receive<Update> {
        return newReceiveBuilder()
            .onMessage(Update::class.java) {msg ->
                context.log.info(msg.message.text)
                val chatId = msg.message.chatId.toString()
                if (!userActorsMap.containsKey(chatId)) {
                    val behavior = EventSourcedUserActor.create(chatId, englishBot)
                    val actorRef = context.spawn(behavior, "userActor_$chatId")
                    userActorsMap[chatId] = actorRef
                }
                userActorsMap.getValue(chatId).tell(TelegramMessage(msg))

                return@onMessage this
            }
            .build()
    }

    companion object {
        fun createBehavior(englishBot: EnglishBot): Behavior<Update> {
            return Behaviors.setup { context ->
                BotSupervisor(englishBot, context)}
        }
    }
}