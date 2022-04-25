package com.blackmorse.telegrambotenglish.akka

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Receive
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.messages.Commands
import com.blackmorse.telegrambotenglish.akka.messages.TelegramMessage
import com.blackmorse.telegrambotenglish.akka.messages.UserActorMessage
import org.telegram.telegrambots.meta.api.objects.Update

abstract class AbstractUserBehavior(
    protected val englishBot: EnglishBot,
    protected val userData: UserData,
    context: ActorContext<UserActorMessage>?) : AbstractBehavior<UserActorMessage>(context) {

    override fun createReceive(): Receive<UserActorMessage> {
        return newReceiveBuilder()
            .onMessage(TelegramMessage::class.java) { msg ->
                if (msg.update.message.text == Commands.BACK.text) {
                    return@onMessage back()
                } else if (msg.update.message.text == Commands.MAIN_MENU.text) {
                    englishBot.sendCommandsList(userData.chatId)
                    return@onMessage UserCommandsBehavior.create(englishBot, userData)
                }
                return@onMessage receiveUpdate(msg.update)
            }.build()
    }

    protected abstract fun receiveUpdate(update: Update): Behavior<UserActorMessage>

    protected abstract fun back(): Behavior<UserActorMessage>
}