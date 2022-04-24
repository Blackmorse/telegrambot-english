package com.blackmorse.telegrambotenglish.akka

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.dictionaries.ShowDictionariesBehavior
import com.blackmorse.telegrambotenglish.akka.messages.*

data class UserData(val chatId: String, val dictionaries: List<String>)

class UserCommandsBehavior(englishBot: EnglishBot, userData: UserData, context: ActorContext<UserActorMessage>?) : AbstractUserBehavior(englishBot, userData, context) {
    override fun createReceive(): Receive<UserActorMessage>? {
        return newReceiveBuilder()
            .onMessage(TelegramMessage::class.java) {msg ->
                if (msg.update.message.text == MessageParser.TopLevelCommands.SHOW_DICTIONARIES.text) {
                    englishBot.sendDictionariesList(userData.chatId, userData.dictionaries)
                    return@onMessage ShowDictionariesBehavior.create(englishBot, userData)
                } else if (msg.update.message.text == "<< Back") {
                    englishBot.sendCommandsList(userData.chatId)
                    return@onMessage this
                } else {
                    return@onMessage this
                }
            }
            .build()
    }

    companion object {
        fun create(englishBot: EnglishBot, userData: UserData): Behavior<UserActorMessage> {
            return Behaviors.setup { context -> UserCommandsBehavior(englishBot, userData, context) }
        }
    }
}