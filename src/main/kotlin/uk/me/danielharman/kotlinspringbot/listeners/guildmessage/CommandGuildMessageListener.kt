package uk.me.danielharman.kotlinspringbot.listeners.guildmessage

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.factories.CommandFactory
import uk.me.danielharman.kotlinspringbot.factories.ModeratorCommandFactory
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.mappers.toMessageEvent
import uk.me.danielharman.kotlinspringbot.properties.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.services.DiscordActionService
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

@Component
class CommandGuildMessageListener(
    private val moderatorCommandFactory: ModeratorCommandFactory,
    private val commandFactory: CommandFactory,
    private val properties: KotlinBotProperties,
    private val springGuildService: SpringGuildService,
    private val discordService: DiscordActionService
) : ListenerAdapter() {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun onMessageReceived(event: MessageReceivedEvent) {

        val isDeafened = springGuildService.isChannelDeafened(event.guild.id, event.channel.id)

        when {
            event.message.contentStripped.startsWith(properties.commandPrefix) -> {
                if (!isDeafened) runCommand(event.toMessageEvent())
            }

            event.message.contentStripped.startsWith(properties.privilegedCommandPrefix) -> {
                runAdminCommand(event)
            }
        }
    }

    private fun runCommand(event: DiscordMessageEvent) {

        val selfUser = discordService.getSelfUser() as Success

        if (event.author.id == selfUser.value.id || event.author.isBot) {
            logger.info("Not running command as author is me or a bot")
            return
        }

        val cmd = event.content.split(" ")[0].removePrefix(properties.commandPrefix)
        val command = commandFactory.getCommand(cmd)

        if (command == null) {
            event.reply(
                Embeds.infoWithDescriptionEmbedBuilder(
                    "Command not found",
                    "If you are trying to use custom commands like save or saved these are now deprecated, use an alternative bot"),
                false)
            return
        }
        try {
            command.execute(event)
        } catch (e: Exception) {
            event.reply("An internal error occurred while executing the command.", true)
            throw e
        }
    }

    private fun runAdminCommand(event: MessageReceivedEvent) {

        if (event.author.id == event.jda.selfUser.id || event.author.isBot) {
            logger.info("Not running command as author is me or a bot")
            return
        }

        val cmd =
            event.message.contentStripped
                .split(" ")[0]
                .removePrefix(properties.privilegedCommandPrefix)
        val channel = event.channel

        val isModerator = springGuildService.isModerator(event.guild.id, event.author.id)

        if (event.author.id != properties.primaryPrivilegedUserId && isModerator is Failure) {
            channel.sendMessage("You are not an admin!").queue()
            return
        }

        val command = moderatorCommandFactory.getCommand(cmd)
        command.execute(event)
    }
}
