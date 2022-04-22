package com.blackmorse.telegrambotenglish

import akka.actor.typed.ActorSystem
import com.blackmorse.telegrambotenglish.akka.BotSupervisor
import com.blackmorse.telegrambotenglish.akka.SimpleText
import com.blackmorse.telegrambotenglish.akka.SupervisorMessage
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

class EnglishBot(val token: String, val name: String) : TelegramLongPollingBot() {
    lateinit  var system: ActorSystem<SupervisorMessage>

    override fun getBotToken(): String {
        return token
    }

    override fun getBotUsername(): String {
        return name
    }


    override fun onUpdateReceived(update: Update?) {
        if (update!!.hasMessage()) {
            system.tell(SimpleText(update.message.text, update.message.chatId.toString()))
        }
    }

    fun justSendText(text: String, chatId: String) {
        val msg = SendMessage().apply { this.chatId = chatId; this.text = text}
        sendApiMethod(msg)
    }

}

fun main(args: Array<String>) {
    try {
        val token = args[0]
        val botName = args[1]
        val englishBot = EnglishBot(token, botName)

        val system  = ActorSystem.create(BotSupervisor.createBehavior(englishBot), "actorSystem")

        englishBot.system = system
        Thread {
            val botApi = TelegramBotsApi(DefaultBotSession::class.java)

            botApi.registerBot(englishBot)
        }.start()
    } catch (e: Exception) {
        println(e)
    }
}