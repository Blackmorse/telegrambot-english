package com.blackmorse.telegrambotenglish.akka.states.games

import com.blackmorse.telegrambotenglish.akka.Dictionary
import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.states.State

//interface GameState {
//    val chainGamesData: List<GameData>
//}
abstract class GameState<T : GameData>(userData: UserData,
                                       val dictionary: Dictionary,
                                       val gameData: T,
                                       val chainGamesData: List<GameData>) : State(userData) {

    override fun backState(): State {
        return PauseGameState(PauseType.BACK, userData, dictionary, this, chainGamesData)
    }

    override fun mainMenuState(state: State): State {
        return PauseGameState(PauseType.MAIN_MENU ,userData, dictionary, this, chainGamesData)
    }
}