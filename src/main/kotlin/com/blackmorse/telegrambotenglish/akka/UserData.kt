package com.blackmorse.telegrambotenglish.akka

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class UserData(
    @JsonProperty("chatId")
    val chatId: String,
    @JsonProperty("dictionaries")
    val dictionaries: List<Dictionary>)

data class Dictionary(
    @JsonProperty("name")
    val name: String,
    @JsonProperty("words")
    val words: List<WordWithTranslation>) {
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

data class WordWithTranslation(
    @JsonProperty("word")
    val word: String,
    @JsonProperty("translation")
    val translation: String) {
    fun reverse(): WordWithTranslation = WordWithTranslation(translation, word)

    override fun toString(): String {
        return "$word <-> $translation"
    }
}
