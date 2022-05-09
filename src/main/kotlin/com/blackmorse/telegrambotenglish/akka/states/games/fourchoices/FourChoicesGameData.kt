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
        fun init(dictionary: Dictionary, word: WordWithTranslation, random: Random): FourChoicesGameData {
            val translations = mutableSetOf<String>()
            translations.add(word.translation)
            while(translations.size < min(4, dictionary.words.size)) {
                translations.add(dictionary.words[random.nextInt(dictionary.words.size)].translation)
            }

            return FourChoicesGameData(word, translations.shuffled(random).toList())
        }

        fun reverseInit(dictionary: Dictionary, word: WordWithTranslation, random: Random): FourChoicesGameData {
            return init(dictionary.reverse(), word.reverse(), random)
        }
    }
}