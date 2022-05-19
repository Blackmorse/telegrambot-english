package com.blackmorse.telegrambotenglish.akka.states.games.typetranslation

import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.WordWithTranslation
import com.blackmorse.telegrambotenglish.akka.states.State
import com.blackmorse.telegrambotenglish.akka.states.games.GameData
import com.fasterxml.jackson.annotation.JsonProperty

class TypeTranslationGameData(
    @JsonProperty("word")
    val word: WordWithTranslation) : GameData {
    override fun createState(userData: UserData, dictionary: Dictionary, chainGamesData: List<GameData>): State {
        return TypeTranslationGameState(userData, dictionary, this, chainGamesData)
    }

    companion object {
        fun init(word: WordWithTranslation): TypeTranslationGameData {
            return TypeTranslationGameData(word)
        }

        fun reverseInit(word: WordWithTranslation): TypeTranslationGameData {
            return init(word.reverse())
        }
    }
}