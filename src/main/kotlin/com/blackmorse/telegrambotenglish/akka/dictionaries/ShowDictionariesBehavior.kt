package com.blackmorse.telegrambotenglish.akka.dictionaries

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.AbstractUserBehavior
import com.blackmorse.telegrambotenglish.akka.UserCommandsBehavior
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.messages.UserActorMessage
import org.telegram.telegrambots.meta.api.objects.Update

class ShowDictionariesBehavior(englishBot: EnglishBot, userData: UserData, context: ActorContext<UserActorMessage>)
        : AbstractUserBehavior(englishBot, userData, context) {
    override fun receiveUpdate(update: Update): Behavior<UserActorMessage> {
        if (update.message.text == "Add") {
            englishBot.justSendText("Enter dictionary name:", userData.chatId)
            return AddDictionaryBehavior.create(englishBot, userData)
        }
        return this
    }

    override fun back(): Behavior<UserActorMessage> {
        englishBot.sendCommandsList(userData.chatId)
        return UserCommandsBehavior.create(englishBot, userData)
    }

    companion object {
        fun create(englishBot: EnglishBot, userData: UserData): Behavior<UserActorMessage> {
            return Behaviors.setup { context -> ShowDictionariesBehavior(englishBot, userData, context) }
        }
    }
}