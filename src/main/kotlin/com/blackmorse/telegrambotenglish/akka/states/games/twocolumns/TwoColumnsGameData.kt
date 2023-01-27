package com.blackmorse.telegrambotenglish.akka.states.games.twocolumns

import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.WordWithTranslation
import com.blackmorse.telegrambotenglish.akka.states.State
import com.blackmorse.telegrambotenglish.akka.states.games.GameData
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*
import java.util.stream.Collectors
import kotlin.math.min
import kotlin.random.Random

data class TwoColumnsGameData(
    @JsonProperty("words")
    val words: List<WordWithTranslation>,
    @JsonProperty("leftColumn")
    val leftColumn: List<String>,
    @JsonProperty("rightColumn")
    val rightColumn: List<String>,
    @JsonProperty("leftSelectedWord")
    val leftSelectedWord: Optional<String>
) : GameData {
    override fun createState(userData: UserData, chainGamesData: List<GameData>, stateAfterFinish: State): State {
        return TwoColumnsGameState(userData, this, chainGamesData, stateAfterFinish)
    }
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
        fun init(dictionary: Dictionary, random: Random): TwoColumnsGameData {
            val words = dictionary.words
            val indexes = mutableSetOf<Int>()
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

        fun reverseInit(dictionary: Dictionary, random: kotlin.random.Random): TwoColumnsGameData {
            return init(dictionary.reverse(), random)
        }
    }
}