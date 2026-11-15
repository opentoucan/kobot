package uk.me.danielharman.kotlinspringbot.listeners.guildmessage

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.me.danielharman.kotlinspringbot.commands.CommandPermission
import uk.me.danielharman.kotlinspringbot.commands.CommandRegistry
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.mappers.toMessageEvent
import uk.me.danielharman.kotlinspringbot.properties.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

@Component
class CommandGuildMessageListener(
    private val commandRegistry: CommandRegistry,
    private val properties: KotlinBotProperties,
    private val springGuildService: SpringGuildService,
) : ListenerAdapter() {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!event.isFromGuild || event.isWebhookMessage || event.author.isBot) return

        when {
            event.message.contentStripped.startsWith(properties.commandPrefix) -> {
                val isDeafened = springGuildService.isChannelDeafened(event.guild.id, event.channel.id)
                if (!isDeafened) runCommand(event.toMessageEvent())
            }

            event.message.contentStripped.startsWith(properties.privilegedCommandPrefix) -> {
                runAdminCommand(event)
            }
        }
    }

    private fun runCommand(event: DiscordMessageEvent) {
        val cmd = event.content.split(" ")[0].removePrefix(properties.commandPrefix)
        val command = commandRegistry.publicCommand(cmd)

        if (command == null) {
            event.reply(
                Embeds.infoWithDescriptionEmbedBuilder(
                    "Command not found",
                    "If you are trying to use custom commands like save " +
                        "or saved these are now deprecated, use an alternative bot",
                ),
                false,
            )
            return
        }
        try {
            command.execute(event)
        } catch (e: Exception) {
            logger.error(e.message, e)
            event.reply("An internal error occurred while executing the command.", true)
            throw e
        }
    }

    private fun runAdminCommand(event: MessageReceivedEvent) {
        val cmd =
            event.message.contentStripped
                .split(" ")[0]
                .removePrefix(properties.privilegedCommandPrefix)
        val channel = event.channel

        if (!commandRegistry.isAuthorized(CommandPermission.GUILD_MODERATOR, event.author.id, event.guild.id)) {
            channel.sendMessage("You are not an admin!").queue()
            return
        }

        val command = commandRegistry.moderatorCommand(cmd)
        command.execute(event)
    }
}
