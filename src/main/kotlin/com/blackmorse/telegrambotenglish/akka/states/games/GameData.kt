package com.blackmorse.telegrambotenglish.akka.states.games

import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.states.State
import com.blackmorse.telegrambotenglish.akka.states.games.combineletters.CombineLettersGameData
import com.blackmorse.telegrambotenglish.akka.states.games.fourchoices.FourChoicesGameData
import com.blackmorse.telegrambotenglish.akka.states.games.twocolumns.TwoColumnsGameData
import com.blackmorse.telegrambotenglish.akka.states.games.typetranslation.TypeTranslationGameData
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
    JsonSubTypes.Type(value = CombineLettersGameData::class, name = "CombineLettersGameData"),
    JsonSubTypes.Type(value = FourChoicesGameData::class, name = "FourChoicesGameData"),
    JsonSubTypes.Type(value = TwoColumnsGameData::class, name = "TwoColumnsGameData"),
    JsonSubTypes.Type(value = TypeTranslationGameData::class, name = "TypeTranslationGameData"),
)
interface GameData {
    fun createState(userData: UserData, dictionary: Dictionary, chainGamesData: List<GameData>): State
}