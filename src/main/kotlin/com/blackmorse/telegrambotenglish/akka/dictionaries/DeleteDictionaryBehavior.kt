package com.blackmorse.telegrambotenglish.akka.dictionaries

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.AbstractUserBehavior
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.UserActorMessage
import org.telegram.telegrambots.meta.api.objects.Update

class DeleteDictionaryBehavior (englishBot: EnglishBot, userData: UserData, context: ActorContext<UserActorMessage>?)
    : AbstractUserBehavior(englishBot, userData, context) {
    override fun receiveUpdate(update: Update): Behavior<UserActorMessage> {
        val dictNameSplit = update.message.text.split(". ", limit = 2)

        if (dictNameSplit.size == 2 && userData.dictionaries.contains(dictNameSplit[1])) {
            val dictName = dictNameSplit[1]
            val newUserData = userData.copy(dictionaries = userData.dictionaries - dictName)
            englishBot.sendDictionariesList(newUserData.chatId, newUserData.dictionaries, true)
            return ShowDictionariesBehavior.create(englishBot, newUserData)
        }
        englishBot.justSendText("There is no dictionary ${update.message.text}", userData.chatId)
        englishBot.sendDictionariesList(userData.chatId, userData.dictionaries, false)
        return this
    }

    override fun back(): Behavior<UserActorMessage> {
        englishBot.sendDictionariesList(userData.chatId, userData.dictionaries, true)
        return ShowDictionariesBehavior.create(englishBot, userData)
    }

    companion object {
        fun create(englishBot: EnglishBot, userData: UserData): Behavior<UserActorMessage> {
            return Behaviors.setup { context -> DeleteDictionaryBehavior(englishBot, userData, context) }
        }
    }
}