package com.blackmorse.telegrambotenglish.akka.states.games.fourchoices

import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.WordWithTranslation
import com.blackmorse.telegrambotenglish.akka.states.State
import com.blackmorse.telegrambotenglish.akka.states.games.GameData
import kotlin.math.min
import kotlin.random.Random

data class FourChoicesGameData(val word: WordWithTranslation,
                                val translations: List<String>) : GameData {
    override fun createState(userData: UserData, dictionary: Dictionary, chainGamesData: List<GameData>): State {
        return FourChoicesGameState(userData, dictionary, this, chainGamesData)
    }

    companion object {
        fun init(dictionary: Dictionary): FourChoicesGameData {
            val random = Random(System.nanoTime())
            val word = dictionary.words[random.nextInt(dictionary.words.size)]

            val translations = mutableSetOf<String>()
            translations.add(word.translation)
            while(translations.size < min(4, dictionary.words.size)) {
                translations.add(dictionary.words[random.nextInt(dictionary.words.size)].translation)
            }

            return FourChoicesGameData(word, translations.toList())
        }
    }
}