package uk.me.danielharman.kotlinspringbot

import akka.actor.ActorRef
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.ApplicationLogger.logger
import uk.me.danielharman.kotlinspringbot.actors.ActorProvider
import uk.me.danielharman.kotlinspringbot.security.DashboardUser
import uk.me.danielharman.kotlinspringbot.security.DashboardUserRepository
import java.util.*
import javax.annotation.PostConstruct

@Component
@Profile("!test")
class SetupService(var actorProvider: ActorProvider, val userRepository: DashboardUserRepository) {

    @PostConstruct
    fun setup() {
        logger.info("Setting up")

        val defaultUser = userRepository.findByUsername("admin")

        //Setup dashboard user
        if (defaultUser == null) {
            logger.info("########################")
            logger.info("No default user found. Creating a new user.")
            val defaultUsername = "admin"
            val randomUUID = UUID.randomUUID().toString()
            logger.info("Username: $defaultUsername Password: $randomUUID")
            userRepository.save(DashboardUser("admin", BCryptPasswordEncoder().encode(randomUUID)))
            logger.info("########################")
        }

        val createActor: ActorRef? = actorProvider.createActor("discordActor", "discord-actor")
        createActor?.tell("start", ActorRef.noSender())
    }

}