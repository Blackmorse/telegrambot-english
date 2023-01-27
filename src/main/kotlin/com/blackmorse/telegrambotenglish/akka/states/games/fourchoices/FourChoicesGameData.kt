package com.blackmorse.telegrambotenglish.akka.states.games.fourchoices

import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.WordWithTranslation
import com.blackmorse.telegrambotenglish.akka.states.State
import com.blackmorse.telegrambotenglish.akka.states.games.GameData
import com.fasterxml.jackson.annotation.JsonProperty
import kotlin.math.min
import kotlin.random.Random

data class FourChoicesGameData(
    @JsonProperty("word")
    val word: WordWithTranslation,
    @JsonProperty("translations")
    val translations: List<String>) : GameData {
    override fun createState(userData: UserData, chainGamesData: List<GameData>, stateAfterFinish: State): State {
        return FourChoicesGameState(userData, this, chainGamesData, stateAfterFinish)
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