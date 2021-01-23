package uk.me.danielharman.kotlinspringbot

import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.objects.ApplicationLogger.logger
import uk.me.danielharman.kotlinspringbot.security.DashboardUser
import uk.me.danielharman.kotlinspringbot.security.DashboardUserRepository
import uk.me.danielharman.kotlinspringbot.services.*
import uk.me.danielharman.kotlinspringbot.objects.DiscordObject
import java.util.*
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
@Profile("!test")
class SetupService(
    val userRepository: DashboardUserRepository,
    val env: Environment, val mongoOperations: MongoOperations,
    val guildService: GuildService,
    val adminCommandService: AdminCommandService,
    val commandService: CommandService,
    val memeService: MemeService,
    val properties: KotlinBotProperties,
    val discordService: DiscordService
) {

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
            if (!DiscordObject.initialised) {
                DiscordObject.init(
                    guildService,
                    adminCommandService,
                    commandService,
                    memeService,
                    properties
                )
            }

            Timer().scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    discordService.sendLatestXkcd()
                }
            }, 3000, 10800000) // Start after 3 seconds, check every 3hrs

        } else {
            logger.info("Running with Discord disabled")
        }

    }

    @PreDestroy
    fun destroy() {
        logger.info("Cleaning up for shutdown")
        DiscordObject.destroy()
        logger.info("Cleanup complete")
    }

}