package com.blackmorse.telegrambotenglish.akka

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.blackmorse.telegrambotenglish.akka.messages.UserActorMessage


class UserHelloBehavior (englishBot: EnglishBot, userData: UserData, context: ActorContext<UserActorMessage>?) :
        AbstractUserBehavior(englishBot, userData, context) {
    override fun createReceive(): Receive<UserActorMessage> {
        return newReceiveBuilder()
            .onMessage(TelegramMessage::class.java) { msg ->
                englishBot.sendCommandsList(userData.chatId)
                UserCommandsBehavior.create(englishBot, userData)
            }.build()
    }

    companion object {
        fun create(englishBot: EnglishBot, userData: UserData): Behavior<UserActorMessage> {
            return Behaviors.setup { context -> UserHelloBehavior(englishBot, userData, context) }
        }
    }
}