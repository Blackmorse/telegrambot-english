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

class AddDictionaryBehavior(englishBot: EnglishBot, userData: UserData, context: ActorContext<UserActorMessage>?)
        : AbstractUserBehavior(englishBot, userData, context){
    override fun createReceive(): Receive<UserActorMessage> {
        return newReceiveBuilder()
            .onMessage(TelegramMessage::class.java) {msg ->
                val dictionaryName = msg.update.message.text
                val newDictionaries = userData.dictionaries + listOf(dictionaryName)
                englishBot.sendDictionariesList(userData.chatId, newDictionaries)
                ShowDictionariesBehavior.create(englishBot, userData.copy(dictionaries = newDictionaries))
            }.build()

    }

    companion object {
        fun create(englishBot: EnglishBot, userData: UserData): Behavior<UserActorMessage> {
            return Behaviors.setup { context -> AddDictionaryBehavior(englishBot, userData, context) }
        }
    }
}