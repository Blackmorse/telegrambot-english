package com.blackmorse.telegrambotenglish.akka

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.dictionaries.ShowDictionariesBehavior
import com.blackmorse.telegrambotenglish.akka.messages.*
import org.telegram.telegrambots.meta.api.objects.Update

data class UserData(val chatId: String, val dictionaries: List<String>)

class UserCommandsBehavior(englishBot: EnglishBot, userData: UserData, context: ActorContext<UserActorMessage>?)
        : AbstractUserBehavior(englishBot, userData, context) {
    override fun receiveUpdate(update: Update): Behavior<UserActorMessage> {
        if (update.message.text == Commands.SHOW_DICTIONARIES.text) {
            englishBot.sendDictionariesList(userData.chatId, userData.dictionaries, true)
            return ShowDictionariesBehavior.create(englishBot, userData)
        }
        return this
    }

    override fun back(): Behavior<UserActorMessage> {
        return this
    }

    companion object {
        fun create(englishBot: EnglishBot, userData: UserData): Behavior<UserActorMessage> {
            return Behaviors.setup { context -> UserCommandsBehavior(englishBot, userData, context) }
        }
    }
}