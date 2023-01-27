package com.blackmorse.telegrambotenglish.akka.states.games.combineletters

import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.WordWithTranslation
import com.blackmorse.telegrambotenglish.akka.states.State
import com.blackmorse.telegrambotenglish.akka.states.games.GameData
import com.fasterxml.jackson.annotation.JsonProperty
import kotlin.random.Random

data class CombineLettersGameData(
    @JsonProperty("word")
    val word: WordWithTranslation,
    @JsonProperty("mixedLetters")
    val mixedLetters: List<Char>,
    @JsonProperty("selectedChars")
    val selectedChars: List<Char>) : GameData {
    override fun createState(userData: UserData, chainGamesData: List<GameData>, stateAfterFinish: State): State {
        return CombineLettersGameState(userData, this, chainGamesData, stateAfterFinish)
    }

    companion object {
        fun init(word: WordWithTranslation, random: Random): CombineLettersGameData {
            val mixedLetters = word.translation.toCharArray().apply{ this.shuffle(random) }.toList()

            return CombineLettersGameData(word, mixedLetters, emptyList())
        }

        fun reverseInit(word: WordWithTranslation, random: Random): CombineLettersGameData {
            return init(word.reverse(), random)
        }
    }
}