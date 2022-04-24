package com.blackmorse.telegrambotenglish.akka.messages

import org.telegram.telegrambots.meta.api.objects.Update

object MessageParser {
    enum class TopLevelCommands(val text: String) {
        SHOW_DICTIONARIES("Show dictionaries"),
        SHOW_DEFERRED_WORDS("Show deferred words")
    }

    fun parseMessage(message: Update): UserActorMessage {
        val chatId = message.message.chatId.toString()
        val text = message.message.text
        if (TopLevelCommands.SHOW_DICTIONARIES.text == text) {
            return ShowDictionariesCommand(chatId)
        } else if (TopLevelCommands.SHOW_DEFERRED_WORDS.text == text) {
            return ShowDeferredWordsCommand(chatId)
        } else {
            throw java.lang.IllegalArgumentException("Illegal command")
        }
    }
}