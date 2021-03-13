package uk.me.danielharman.kotlinspringbot.objects

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.factories.AdminCommandFactory
import uk.me.danielharman.kotlinspringbot.factories.CommandFactory
import uk.me.danielharman.kotlinspringbot.factories.VoiceCommandFactory
import uk.me.danielharman.kotlinspringbot.listeners.MessageListener
import uk.me.danielharman.kotlinspringbot.services.*

object DiscordObject {

    lateinit var jda: JDA
    var initialised: Boolean = false
    var startTime: DateTime? = null
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun init(
        guildService: GuildService,
        adminCommandFactory: AdminCommandFactory,
        commandFactory: CommandFactory,
        voiceCommandFactory: VoiceCommandFactory,
        memeService: MemeService,
        properties: KotlinBotProperties
    ) {

        logger.info("Starting discord")

        val builder: JDABuilder = JDABuilder.create(
            properties.token,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_PRESENCES,
            GatewayIntent.DIRECT_MESSAGES,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_EMOJIS,
            GatewayIntent.GUILD_MESSAGE_REACTIONS
        )
            .setActivity(Activity.of(Activity.ActivityType.DEFAULT, "${properties.commandPrefix}help"))
            .addEventListeners(
                MessageListener(
                    guildService,
                    adminCommandFactory,
                    commandFactory,
                    voiceCommandFactory,
                    properties,
                    memeService
                )
            )

        initialised = true
        startTime = DateTime.now()
        jda = builder.build().awaitReady()
    }

    fun teardown() {
        logger.info("Discord teardown")
        if (initialised) {
            jda.shutdown()
        }
        initialised = false
        startTime = null
    }
}