package com.blackmorse.telegrambotenglish

import akka.actor.typed.ActorSystem
import com.blackmorse.telegrambotenglish.akka.BotSupervisor
import com.blackmorse.telegrambotenglish.akka.messages.Commands
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

class EnglishBot(val token: String, val name: String) : TelegramLongPollingBot() {
    lateinit  var system: ActorSystem<Update>

    override fun getBotToken(): String {
        return token
    }

    override fun getBotUsername(): String {
        return name
    }


    override fun onUpdateReceived(update: Update?) {
        if (update!!.hasMessage()) {
            system.tell(update)
        }
    }

    private fun createMessage(chatId: String, text: String, builder: ReplyKeyboardMarkupBuilder = ReplyKeyboardMarkup.builder()): SendMessage {
        builder.keyboardRow(KeyboardRow(listOf(KeyboardButton(Commands.BACK.text), KeyboardButton(Commands.MAIN_MENU.text))))

        return SendMessage().apply { this.chatId = chatId; this.replyMarkup = builder.build(); this.text = text }
    }

    fun justSendText(text: String, chatId: String) {
        val msg = createMessage(chatId, text)
        sendApiMethod(msg)
    }

    fun sendCommandsList(chatId: String) {
        val builder = ReplyKeyboardMarkup.builder()
        builder.keyboardRow(KeyboardRow(listOf(KeyboardButton(Commands.SHOW_DICTIONARIES.text))))
        val msg = createMessage(chatId, "please select", builder)
        sendApiMethod(msg)
    }

    fun sendDictionariesList(chatId: String, dictsList: List<String>) {
        val chunkedDictionaries = dictsList.chunked(3)
        val builder = ReplyKeyboardMarkup.builder()
        chunkedDictionaries.forEach{ dictionaries ->
            builder.keyboardRow(KeyboardRow(dictionaries.map { KeyboardButton(it) }))
        }
        builder.keyboardRow(KeyboardRow(listOf(KeyboardButton("Add"), KeyboardButton("Delete"))))

        val text = dictsList.withIndex().joinToString("\n") { "${it.index}. ${it.value}" }
        val msg = createMessage(chatId, "Select dictionary or actions: \n$text", builder)
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