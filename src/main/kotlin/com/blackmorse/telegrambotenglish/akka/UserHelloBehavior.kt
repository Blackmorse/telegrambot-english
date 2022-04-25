package com.blackmorse.telegrambotenglish.akka

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.messages.UserActorMessage
import org.telegram.telegrambots.meta.api.objects.Update


class UserHelloBehavior (englishBot: EnglishBot, userData: UserData, context: ActorContext<UserActorMessage>?) :
        AbstractUserBehavior(englishBot, userData, context) {
    override fun receiveUpdate(update: Update): Behavior<UserActorMessage> {
        englishBot.sendCommandsList(userData.chatId)
        return UserCommandsBehavior.create(englishBot, userData)
    }

    override fun back(): Behavior<UserActorMessage> {
        return this
    }

    companion object {
        fun create(englishBot: EnglishBot, userData: UserData): Behavior<UserActorMessage> {
            return Behaviors.setup { context -> UserHelloBehavior(englishBot, userData, context) }
        }
    }
}