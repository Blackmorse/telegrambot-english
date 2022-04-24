package com.blackmorse.telegrambotenglish.akka

import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import com.blackmorse.telegrambotenglish.EnglishBot
import com.blackmorse.telegrambotenglish.akka.messages.UserActorMessage

abstract class AbstractUserBehavior(
    protected val englishBot: EnglishBot,
    protected val userData: UserData,
    context: ActorContext<UserActorMessage>?) : AbstractBehavior<UserActorMessage>(context) {
}