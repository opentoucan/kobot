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
import uk.me.danielharman.kotlinspringbot.listeners.MessageListener
import uk.me.danielharman.kotlinspringbot.services.GuildService
import uk.me.danielharman.kotlinspringbot.services.RequestService


@Component
@Scope("prototype")
class DiscordActor(val guildService: GuildService, val requestService: RequestService) : UntypedAbstractActor() {

    private lateinit var jda: JDA

    @Value("\${discord.token}")
    private lateinit var token: String

    @Value("\${discord.commandPrefix}")
    private lateinit var prefix: String

    @Value("\${discord.privilegedCommandPrefix}")
    private lateinit var privilegedCommandPrefix: String

    @Value("\${discord.primaryPrivilegedUserId}")
    private lateinit var primaryPrivilegedUserId: String

    override fun onReceive(message: Any?) = when (message) {
        "start" -> start()
        else -> println("received unknown message")
    }

    fun start() {

        logger.info("Starting discord")

        val builder: JDABuilder = JDABuilder.create(
                token,
                GUILD_MEMBERS,
                GUILD_PRESENCES,
                DIRECT_MESSAGES,
                GUILD_MESSAGES,
                GUILD_VOICE_STATES,
                GUILD_EMOJIS,
                GUILD_MESSAGE_REACTIONS)
                .setActivity(Activity.of(Activity.ActivityType.DEFAULT, "${prefix}help"))
                .addEventListeners(MessageListener(guildService, prefix, privilegedCommandPrefix,
                        primaryPrivilegedUserId, requestService))

        jda = builder.build().awaitReady()

        logger.debug("End")
    }

}

