package com.blackmorse.telegrambotenglish.akka

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.messages.MessageParser
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.blackmorse.telegrambotenglish.akka.messages.UserActorMessage
import org.telegram.telegrambots.meta.api.objects.Update

sealed class SupervisorMessage

data class SimpleText(val text: String, val chatId: String) : SupervisorMessage()



class BotSupervisor(private val englishBot: EnglishBot,
                    context: ActorContext<Update>?) : AbstractBehavior<Update>(context) {
    init {
        context?.log?.info("Actor System for English Bot has started")
    }

    private val userActorsMap = HashMap<String, ActorRef<UserActorMessage>>()
    override fun createReceive(): Receive<Update> {
        return newReceiveBuilder()
            .onMessage(Update::class.java) {msg ->
                val chatId = msg.message.chatId.toString()


                if (!userActorsMap.containsKey(chatId)) {
                    val userData = UserData(chatId, emptyList())
                    val behavior = UserHelloBehavior.create(englishBot, userData)
                    val actorRef = context.spawn(behavior, "userActor_$chatId")
                    userActorsMap[chatId] = actorRef
//                    actorRef.tell(HelloUser)
                }

                userActorsMap.getValue(chatId).tell(TelegramMessage(msg))
//                    val behavior = UserCommandsBehavior.create(englishBot, userData)
//                    val actorRef = context.spawn(behavior, "userActor$chatId")
//                    userActorsMap[chatId] = actorRef
//                    englishBot.sendCommandsList(chatId)
//                } else {
//                    val actorRef = userActorsMap.getValue(chatId)
//                    val akkaMessage = MessageParser.parseMessage(msg)
//
//                    actorRef.tell(akkaMessage)
//                }
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