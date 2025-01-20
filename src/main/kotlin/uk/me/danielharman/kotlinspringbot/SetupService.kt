package uk.me.danielharman.kotlinspringbot

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.admin.enums.Role
import uk.me.danielharman.kotlinspringbot.objects.ApplicationInfo
import uk.me.danielharman.kotlinspringbot.objects.DiscordObject
import uk.me.danielharman.kotlinspringbot.services.*
import uk.me.danielharman.kotlinspringbot.services.admin.AdministratorService
import java.time.LocalDateTime

@Component
@Profile("!test")
class SetupService(
    private val env: Environment,
    private val mongoOperations: MongoOperations,
    private val discordService: DiscordService,
    private val buildProperties: BuildProperties,
    private val kotlinBotProperties: KotlinBotProperties,
    private val administratorService: AdministratorService,
    private val listeners: List<ListenerAdapter>
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun setup() {
        logger.info("Running bot version ${buildProperties.version}")

        SchemaUpdater(mongoOperations).updateSchema()

        val activeProfiles = env.activeProfiles

        ApplicationInfo.isDev = activeProfiles.contains("dev")

        if(ApplicationInfo.isDev){
            logger.info("Bot is running in development mode.")
        }

        //Create default admin
        administratorService.createBotAdministrator(kotlinBotProperties.primaryPrivilegedUserId, setOf(Role.Primary))

        if (!activeProfiles.contains("discordDisabled")) {
            //Injecting listeners here otherwise we'll get circular dependencies
            DiscordObject.registerListeners(listeners)

            when(val dc = discordService.startDiscordConnection())
            {
                is Failure -> logger.error("Discord connection failed to start ${dc.reason}")
                is Success -> {
                    logger.info(dc.value)
                    administratorService.logToAdmins("Bot started")
                    administratorService.logToAdmins("Syncing guilds with database")
                    discordService.syncGuildsWithDb()
                }
            }
        } else {
            logger.info("Running with Discord disabled")
        }

        //Kotlin objects are lazy
        ApplicationInfo.startTime = LocalDateTime.now()
        ApplicationInfo.version = buildProperties.version
    }

    @PreDestroy
    fun destroy() {
        logger.info("Cleaning up for shutdown")
        when(val c = discordService.closeDiscordConnection()){
            is Failure -> logger.error(c.reason)
            is Success -> logger.info(c.value)
        }
        logger.info("Cleanup complete")
    }

}