package com.blackmorse.telegrambotenglish.akka.states.games.combineletters

import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.WordWithTranslation
import com.blackmorse.telegrambotenglish.akka.states.State
import com.blackmorse.telegrambotenglish.akka.states.games.GameData
import kotlin.random.Random

data class CombineLettersGameData(val word: WordWithTranslation,
                                val mixedLetters: List<Char>, val selectedChars: List<Char>) : GameData {
    override fun createState(userData: UserData, dictionary: Dictionary, chainGamesData: List<GameData>): State {
        return CombineLettersGameState(userData, dictionary, this, chainGamesData)
    }

    companion object {
        fun init(word: WordWithTranslation, random: Random): CombineLettersGameData {
            val mixedLetters = word.translation.toCharArray().apply{ this.shuffle(random) }.toList()

            return CombineLettersGameData(word, mixedLetters, emptyList())
        }
    }
}