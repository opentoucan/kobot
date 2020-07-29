package uk.me.danielharman.kotlinspringbot

import akka.actor.ActorRef
import akka.actor.ActorSystem
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.ApplicationLogger.logger
import uk.me.danielharman.kotlinspringbot.actors.ActorProvider
import uk.me.danielharman.kotlinspringbot.security.DashboardUser
import uk.me.danielharman.kotlinspringbot.security.DashboardUserRepository
import java.util.*
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
@Profile("!test")
class SetupService(var actorProvider: ActorProvider, val userRepository: DashboardUserRepository,
                   val env: Environment, val mongoOperations: MongoOperations, val actorSystem: ActorSystem) {

    @PostConstruct
    fun setup() {
        logger.info("Setting up")

        SchemaUpdater(mongoOperations).updateSchema()

        val activeProfiles = env.activeProfiles

        val defaultUser = userRepository.findByUsername("admin")
        //Setup dashboard user
        if (defaultUser == null) {
            logger.info("########################")
            logger.info("No default user found. Creating a new user.")
            val defaultUsername = "admin"
            val randomUUID = UUID.randomUUID().toString()
            logger.info("Username: $defaultUsername Password: $randomUUID")
            userRepository.save(DashboardUser(defaultUsername, BCryptPasswordEncoder().encode(randomUUID)))
            logger.info("########################")
        }

        if (activeProfiles.contains("dev")) {
            val devUser = userRepository.findByUsername("dev")
            //Setup dashboard user
            if (devUser == null) {
                logger.info("########################")
                logger.info("No dev user found. Creating a new user.")
                val defaultUsername = "dev"
                val password = "password"
                logger.info("Username: $defaultUsername Password: $password")
                userRepository.save(DashboardUser(defaultUsername, BCryptPasswordEncoder().encode(password)))
                logger.info("########################")
            }
        }

        if (!activeProfiles.contains("discordDisabled")) {
            logger.info("Creating discord actor")
            val discordActor = actorProvider.createActor("discordActor", "discord-actor")

            discordActor?.tell("start", ActorRef.noSender())
                    ?: logger.error("Failed to start Discord actor")

//            actorSystem.scheduler().schedule(Duration.ofSeconds(10), Duration.ofSeconds(20), discordActor,
//                    "xkcd", actorSystem.dispatcher(), ActorRef.noSender())

        } else {
            logger.info("Running with Discord disabled")
        }



    }

    @PreDestroy
    fun destroy() {
        logger.info("Cleaning up for shutdown")
        actorProvider.getActor("discord-actor")?.tell("stop", ActorRef.noSender())
        logger.info("Cleanup complete")
    }

}