package com.blackmorse.telegrambotenglish.akka.dictionaries

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.AbstractUserBehavior
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.blackmorse.telegrambotenglish.akka.messages.UserActorMessage

class ShowDictionariesBehavior(englishBot: EnglishBot, userData: UserData, context: ActorContext<UserActorMessage>)
        : AbstractUserBehavior(englishBot, userData, context) {
    override fun createReceive(): Receive<UserActorMessage> {
        return newReceiveBuilder()
            .onMessage(TelegramMessage::class.java) {msg ->
                if (msg.update.message.text == "Add") {
                    englishBot.justSendText("Enter dictionary name:", userData.chatId)
                }
                AddDictionaryBehavior.create(englishBot, userData)
            }
            .build()
    }

    companion object {
        fun create(englishBot: EnglishBot, userData: UserData): Behavior<UserActorMessage> {
            return Behaviors.setup { context -> ShowDictionariesBehavior(englishBot, userData, context) }
        }
    }
}