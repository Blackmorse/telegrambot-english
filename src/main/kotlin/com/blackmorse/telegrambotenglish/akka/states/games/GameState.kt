package com.blackmorse.telegrambotenglish.akka.states.games

import com.blackmorse.telegrambotenglish.akka.UserData
import com.blackmorse.telegrambotenglish.akka.states.State
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
abstract class GameState<T : GameData>(userData: UserData,
                                       val gameData: T,
                                       val chainGamesData: List<GameData>,
                                       val stateAfterFinish: State) : State(userData) {

    override fun backState(): State {
        return PauseGameState(PauseType.BACK, userData, this, stateAfterFinish, chainGamesData)
    }

    override fun mainMenuState(state: State): State {
        return PauseGameState(PauseType.MAIN_MENU, userData, this, stateAfterFinish, chainGamesData)
    }

    protected fun finishGame(): State {
        return if (chainGamesData.isEmpty()) {
            stateAfterFinish
        } else {
            chainGamesData[0].createState(userData, chainGamesData - chainGamesData[0], stateAfterFinish)
        }
    }
}