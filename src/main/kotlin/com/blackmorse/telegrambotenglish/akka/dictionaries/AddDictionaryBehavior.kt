package com.blackmorse.telegrambotenglish.akka.dictionaries

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.AbstractUserBehavior
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.UserActorMessage
import org.telegram.telegrambots.meta.api.objects.Update

class AddDictionaryBehavior(englishBot: EnglishBot, userData: UserData, context: ActorContext<UserActorMessage>?)
        : AbstractUserBehavior(englishBot, userData, context){
    override fun receiveUpdate(update: Update): Behavior<UserActorMessage> {
        val dictionaryName = update.message.text
        val newDictionaries = userData.dictionaries + listOf(dictionaryName)
        englishBot.sendDictionariesList(userData.chatId, newDictionaries, true)
        return ShowDictionariesBehavior.create(englishBot, userData.copy(dictionaries = newDictionaries))
    }

    override fun back(): Behavior<UserActorMessage> {
        englishBot.sendDictionariesList(userData.chatId, userData.dictionaries, true)
        return ShowDictionariesBehavior.create(englishBot, userData)
    }

    companion object {
        fun create(englishBot: EnglishBot, userData: UserData): Behavior<UserActorMessage> {
            return Behaviors.setup { context -> AddDictionaryBehavior(englishBot, userData, context) }
        }
    }
}