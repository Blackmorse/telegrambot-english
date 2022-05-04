package com.blackmorse.telegrambotenglish.akka.states.games

import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.WordWithTranslation
import java.util.*
import java.util.stream.Collectors
import kotlin.math.min
import kotlin.random.Random

data class TwoColumnsGameData(val words: List<WordWithTranslation>,
                              val leftColumn: List<String>,
                              val rightColumn: List<String>,
                              val leftSelectedWord: Optional<String>
) {
    fun checkRightWord(word: String): Optional<WordWithTranslation> {
        if (!leftSelectedWord.isPresent) return Optional.empty()
        val leftWord = leftSelectedWord.get()
        val wordWithTranslation = words.find { it.word == leftWord }!!
        return if (wordWithTranslation.translation == word) {
            Optional.of(wordWithTranslation)
        } else {
            Optional.empty()
        }
    }

    companion object {
        fun init(dictionary: Dictionary): TwoColumnsGameData {
            val words = dictionary.words
            val indexes = mutableSetOf<Int>()
            val random = Random(System.currentTimeMillis())
            while (indexes.size < min(4, words.size)) {
                indexes.add(random.nextInt(words.size))
            }
            val selectedWords = indexes.stream().map { words[it] }.collect(Collectors.toList())

            val leftColumn = selectedWords.map { it.word }.shuffled(random)
            val rightColumn = selectedWords.map { it.translation }.shuffled(random)

            return TwoColumnsGameData(words = selectedWords,
                leftColumn = leftColumn,
                rightColumn= rightColumn,
                leftSelectedWord = Optional.empty())
        }
    }
}