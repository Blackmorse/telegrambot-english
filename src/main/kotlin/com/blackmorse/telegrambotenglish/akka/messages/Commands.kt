package com.blackmorse.telegrambotenglish.akka.messages

enum class Commands(val text: String) {
    BACK("<< BACK"),
    MAIN_MENU("Main Menu"),
    SHOW_DICTIONARIES("Show dictionaries"),
    ADD_DICTIONARY("Add Dictionary"),
    DELETE_DICTIONARY("Delete Dictionary"),
    IMPORT_DICTIONARY("Import Dictionary"),
    ADD_WORD("Add Word"),
    DELETE_WORD("Delete Word"),
    START_GAME("Start game")
}
