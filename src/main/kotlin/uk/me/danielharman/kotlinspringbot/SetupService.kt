package uk.me.danielharman.kotlinspringbot

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
    }

}