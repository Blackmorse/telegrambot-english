package com.blackmorse.telegrambotenglish

import akka.actor.typed.ActorSystem
import com.blackmorse.telegrambotenglish.akka.BotSupervisor
import com.blackmorse.telegrambotenglish.akka.SimpleText
import com.blackmorse.telegrambotenglish.akka.SupervisorMessage
import com.blackmorse.telegrambotenglish.akka.messages.MessageParser
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard.ForceReplyKeyboardBuilder
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
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

    fun justSendText(text: String, chatId: String) {
        val msg = SendMessage().apply { this.chatId = chatId; this.text = text}

        sendApiMethod(msg)
    }

    fun sendCommandsList(chatId: String) {
        val builder = ReplyKeyboardMarkup.builder()
        MessageParser.TopLevelCommands.values()
            .forEach { builder.keyboardRow(KeyboardRow(listOf(KeyboardButton(it.text)))) }

        val keyboard = builder.build()

        val msg = SendMessage().apply { this.chatId = chatId; this.replyMarkup = keyboard; this.text = "please select" }

        sendApiMethod(msg)
    }

    fun sendDictionariesList(chatId: String, dictsList: List<String>) {
        val chunkedDictionaries = dictsList.chunked(3)
        val builder = ReplyKeyboardMarkup.builder()
        chunkedDictionaries.forEach{ dictionaries ->
            builder.keyboardRow(KeyboardRow(dictionaries.map { KeyboardButton(it) }))
        }
        builder.keyboardRow(KeyboardRow(listOf(KeyboardButton("<< Back"), KeyboardButton("Add"), KeyboardButton("Delete"))))

        val keyboard = builder.build()

        val text = dictsList.withIndex().map { "${it.index}. ${it.value}" }.joinToString("\n")

        val msg = SendMessage().apply { this.chatId = chatId; this.replyMarkup = keyboard; this.text = "Select dictionary or action: \n $text" }

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