package com.blackmorse.telegrambotenglish.akka.states.games

import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.states.State

interface GameData {
    fun createState(userData: UserData, dictionary: Dictionary, chainGamesData: List<GameData>): State
}