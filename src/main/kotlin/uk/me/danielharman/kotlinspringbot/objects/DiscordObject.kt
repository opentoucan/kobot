package uk.me.danielharman.kotlinspringbot.objects

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties

object DiscordObject {

    lateinit var jda: JDA
    var initialised: Boolean = false
    var startTime: DateTime? = null
    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    var listeners: List<ListenerAdapter> = listOf()

    fun init(
        properties: KotlinBotProperties
    ) {

        logger.info("Starting discord")
        logger.info("${listeners.size} listeners registered")
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

        for (listener: ListenerAdapter in listeners) {
            builder.addEventListeners(listener)
        }

        initialised = true
        startTime = DateTime.now()
        jda = builder.build().awaitReady()
    }

    fun registerListeners(listeners: List<ListenerAdapter>){
        this.listeners = listeners
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