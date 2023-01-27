package com.blackmorse.telegrambotenglish

import akka.actor.typed.ActorSystem
import akka.actor.typed.LogOptions
import akka.actor.typed.javadsl.Behaviors
import com.blackmorse.telegrambotenglish.akka.BotSupervisor
import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.messages.Commands
import com.blackmorse.telegrambotenglish.akka.states.games.combineletters.CombineLettersGameData
import com.blackmorse.telegrambotenglish.akka.states.games.fourchoices.FourChoicesGameData
import com.blackmorse.telegrambotenglish.akka.states.games.twocolumns.TwoColumnsGameData
import org.slf4j.event.Level
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

class EnglishBot(private val token: String, private val name: String) : TelegramLongPollingBot() {
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
        builder.keyboardRow(KeyboardRow(listOf(
                KeyboardButton(Commands.SHOW_DICTIONARIES.text),
                KeyboardButton(Commands.WORD_OF_THE_DAY.text)
        )))
        val msg = createMessage(chatId, "please select", builder)
        sendApiMethod(msg)
    }

    fun sendDictionaryInfo(chatId: String, dictionary: Dictionary) {
        val wordsText = dictionary.words.withIndex()
            .map { "${it.index}. ${it.value}"}
            .joinToString ( "\n" )

        val text = "${dictionary.name}\n\n$wordsText"

        val builder = ReplyKeyboardMarkup.builder()
        builder.keyboardRow(KeyboardRow(listOf(KeyboardButton(Commands.ADD_WORD.text), KeyboardButton(Commands.DELETE_WORD.text))))
        builder.keyboardRow(KeyboardRow(listOf(KeyboardButton(Commands.START_GAME.text))))
        val msg = createMessage(chatId, text, builder)
        sendApiMethod(msg)
    }

    fun sendItemsList(chatId: String, dictsList: List<String>, showDictCommands: Boolean) {
        val dictsWithIndex = dictsList.withIndex()
        val chunkedDictionaries = dictsWithIndex.chunked(3)
        val builder = ReplyKeyboardMarkup.builder()
        chunkedDictionaries.forEach{ dictionaries ->
            builder.keyboardRow(KeyboardRow(dictionaries.map { KeyboardButton("${it.index + 1}. ${it.value}") }))
        }

        val addButton = KeyboardButton(Commands.ADD_DICTIONARY.text)
        val importButton = KeyboardButton(Commands.IMPORT_DICTIONARY.text)

        if (showDictCommands) {
            val dictionaryActionButtons = if (dictsList.isEmpty()) {
                listOf(addButton, importButton)
            } else {
                listOf(addButton, importButton, KeyboardButton(Commands.DELETE_DICTIONARY.text))
            }
            builder.keyboardRow(KeyboardRow(dictionaryActionButtons))
        }

        val text = dictsWithIndex.joinToString("\n") { "${it.index + 1}. ${it.value}" }
        val msg = createMessage(chatId, "Select dictionary or actions: \n$text", builder)
        sendApiMethod(msg)
    }

    fun sendTwoColumnsGame(chatId: String, gameData: TwoColumnsGameData) {
        val builder = ReplyKeyboardMarkup.builder()

        gameData.leftColumn.zip(gameData.rightColumn).forEach { (left, right) ->
            val prefix = gameData.leftSelectedWord.map { if (it == left) ">> " else "" }.orElseGet { "" }
            val postfix = gameData.leftSelectedWord.map { if (it == left) " << " else "" }.orElseGet { "" }
            builder.keyboardRow(KeyboardRow(listOf(KeyboardButton("$prefix$left$postfix"), KeyboardButton(right))))
        }

        val msg = createMessage(chatId, "Find matches", builder)
        sendApiMethod(msg)
    }

    fun sendFourChoicesGame(chatId: String, gameData: FourChoicesGameData) {
        val builder = ReplyKeyboardMarkup.builder()

        gameData.translations.forEach {
            builder.keyboardRow(KeyboardRow(listOf(KeyboardButton(it))))
        }

        val msg = createMessage(chatId, "Select translation for ${gameData.word.word}:", builder)
        sendApiMethod(msg)
    }

    fun sendConfirmation(chatId: String, text: String) {
        val builder = ReplyKeyboardMarkup.builder()
            .apply {
                this.keyboardRow(KeyboardRow(listOf(KeyboardButton(Commands.YES.text), KeyboardButton(Commands.NO.text))))
            }

        val msg = createMessage(chatId, text, builder)
        sendApiMethod(msg)
    }

    fun sendCombineLettersGameState(chatId: String, gameData: CombineLettersGameData) {
        val builder = ReplyKeyboardMarkup.builder()

        val rows = gameData.mixedLetters.chunked((gameData.mixedLetters.size + 2) / 3)
        rows.forEach {
            builder.keyboardRow(KeyboardRow(it.map { char -> KeyboardButton(
                if (char == ' ') "_"
                else if(char == '+') "\u2705"
                else char.toString()
            ) }))
        }

        val msg = createMessage(chatId, "Select next letter for word ${gameData.word.word}:\n ${gameData.selectedChars.joinToString("")}", builder)
        sendApiMethod(msg)
    }
}

fun main(args: Array<String>) {
    try {
        val token = args[0]
        val botName = args[1]
        val englishBot = EnglishBot(token, botName)

        val system  = ActorSystem.create(
            Behaviors.logMessages(LogOptions.create().withLevel(Level.TRACE),BotSupervisor.createBehavior(englishBot)),
            "actorSystem")

        englishBot.system = system
        Thread {
            val botApi = TelegramBotsApi(DefaultBotSession::class.java)

            botApi.registerBot(englishBot)
        }.start()
    } catch (e: Exception) {
        println(e)
    }
}
