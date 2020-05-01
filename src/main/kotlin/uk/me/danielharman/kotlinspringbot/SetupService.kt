package uk.me.danielharman.kotlinspringbot

import akka.actor.ActorRef
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.ApplicationLogger.logger
import uk.me.danielharman.kotlinspringbot.actors.ActorProvider
import javax.annotation.PostConstruct

@Component
class SetupService(var actorProvider: ActorProvider) {

    @PostConstruct
    fun setup() {
        logger.info("Setting up")
        actorProvider.createActor("testActor", "test")
        val createActor : ActorRef? = actorProvider.createActor("discordActor", "discord-actor")
        createActor?.tell("start", ActorRef.noSender())
    }

}