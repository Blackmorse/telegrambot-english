package com.blackmorse.telegrambotenglish.akka.messages

import org.telegram.telegrambots.meta.api.objects.Update

abstract class UserActorMessage

data class TelegramMessage(val update: Update) : UserActorMessage()

data class ShowDictionariesCommand(val chatId: String) : UserActorMessage()

data class ShowDeferredWordsCommand(val chatId: String) : UserActorMessage()
