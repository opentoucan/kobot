package uk.me.danielharman.kotlinspringbot.actors

import akka.actor.UntypedAbstractActor
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.ApplicationLogger.logger
import uk.me.danielharman.kotlinspringbot.command.CommandProvider
import uk.me.danielharman.kotlinspringbot.listeners.MessageListener
import uk.me.danielharman.kotlinspringbot.services.AdminCommandService
import uk.me.danielharman.kotlinspringbot.services.GuildService


@Component
@Scope("prototype")
class DiscordActor(val guildService: GuildService,
                   val adminCommandService: AdminCommandService,
                   val commandProvider: CommandProvider
) : UntypedAbstractActor() {

    private lateinit var jda: JDA

    @Value("\${discord.token}")
    private lateinit var token: String

    @Value("\${discord.commandPrefix}")
    private lateinit var commandPrefix: String

    @Value("\${discord.privilegedCommandPrefix}")
    private lateinit var adminCommandPrefix: String

    @Value("\${discord.primaryPrivilegedUserId}")
    private lateinit var primaryAdminUserId: String

    override fun onReceive(message: Any?) = when (message) {
        "start" -> start()
        "stop" -> stop()
        "restart" -> restart()
        else -> println("received unknown message")
    }

    fun start() {
        logger.info("Starting discord actor")
        val builder: JDABuilder = JDABuilder.create(
                token,
                GUILD_MEMBERS,
                GUILD_PRESENCES,
                DIRECT_MESSAGES,
                GUILD_MESSAGES,
                GUILD_VOICE_STATES,
                GUILD_EMOJIS,
                GUILD_MESSAGE_REACTIONS)
                .setActivity(Activity.of(Activity.ActivityType.DEFAULT, "${commandPrefix}help"))
                .addEventListeners(MessageListener(guildService, adminCommandService, commandProvider))

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

}

