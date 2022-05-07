package com.blackmorse.telegrambotenglish.akka

import java.util.*

data class UserData(val chatId: String, val dictionaries: List<Dictionary>)

data class Dictionary(val name: String, val words: List<WordWithTranslation>) {
    fun reverse(): Dictionary = Dictionary(name, words.map { it.reverse() })

    companion object {
        fun getItemFromIndexedList(message: String): Optional<String> {
            val dictNameSplit = message.split(". ", limit = 2)
            return if (dictNameSplit.size == 2) {
                Optional.of(dictNameSplit[1])
            } else {
                Optional.empty()
            }
        }
    }
}

data class WordWithTranslation(val word: String, val translation: String) {
    fun reverse(): WordWithTranslation = WordWithTranslation(translation, word)

    override fun toString(): String {
        return "$word <-> $translation"
    }
}
