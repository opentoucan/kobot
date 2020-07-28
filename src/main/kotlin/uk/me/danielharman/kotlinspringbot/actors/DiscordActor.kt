package uk.me.danielharman.kotlinspringbot.actors

import akka.actor.UntypedAbstractActor
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.requests.GatewayIntent.*
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.ApplicationLogger.logger
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.services.CommandService
import uk.me.danielharman.kotlinspringbot.listeners.MessageListener
import uk.me.danielharman.kotlinspringbot.services.AdminCommandService
import uk.me.danielharman.kotlinspringbot.services.GuildService
import uk.me.danielharman.kotlinspringbot.services.MemeService


@Component
@Scope("prototype")
class DiscordActor(val guildService: GuildService,
                   val adminCommandService: AdminCommandService,
                   val commandService: CommandService,
                   val properties: KotlinBotProperties,
                   val memeService: MemeService
) : UntypedAbstractActor() {

    private lateinit var jda: JDA

    override fun onReceive(message: Any?) = when (message) {
        "start" -> start()
        "stop" -> stop()
        "restart" -> restart()
        is DiscordChannelMessage -> sendChannelMessage(message)
        is DiscordChannelEmbedMessage -> sendChannelMessage(message)
        else -> println("received unknown message")
    }

    fun start() {
        logger.info("Starting discord actor")
        val builder: JDABuilder = JDABuilder.create(
                properties.token,
                GUILD_MEMBERS,
                GUILD_PRESENCES,
                DIRECT_MESSAGES,
                GUILD_MESSAGES,
                GUILD_VOICE_STATES,
                GUILD_EMOJIS,
                GUILD_MESSAGE_REACTIONS)
                .setActivity(Activity.of(Activity.ActivityType.DEFAULT, "${properties.commandPrefix}help"))
                .addEventListeners(MessageListener(guildService, adminCommandService, commandService, properties, memeService))

        jda = builder.build().awaitReady()
    }

    fun stop() {
        logger.info("Shutting down Discord")
        jda.shutdown()
        logger.info("Shutdown complete")
    }

    fun restart() {
        logger.info("Attempting to restart discord")
        stop()
        start()
    }

    fun sendChannelMessage(msg : DiscordChannelMessage){
        logger.info("[Discord Actor] Sending message: $msg")
        jda.getTextChannelById(msg.channelId)?.sendMessage(msg.msg)?.queue() ?: logger.error("Could not send message $msg")
    }

    fun sendChannelMessage(msg : DiscordChannelEmbedMessage){
        logger.info("[Discord Actor] Sending message: $msg")
        jda.getTextChannelById(msg.channelId)?.sendMessage(msg.msg)?.queue() ?: logger.error("Could not send message $msg")
    }

    data class DiscordChannelMessage(val msg: String, val guildId: String, val channelId: String)
    data class DiscordChannelEmbedMessage(val msg: MessageEmbed, val channelId: String)
}

