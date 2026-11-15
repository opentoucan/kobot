package uk.me.danielharman.kotlinspringbot.listeners

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.commands.CommandPermission
import uk.me.danielharman.kotlinspringbot.commands.CommandRegistry
import uk.me.danielharman.kotlinspringbot.properties.KotlinBotProperties

@Component
class DirectMessageListener(
    private val commandRegistry: CommandRegistry,
    private val properties: KotlinBotProperties,
) : ListenerAdapter() {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.isFromGuild || event.isWebhookMessage) return

        when {
            event.message.contentStripped.startsWith(properties.privilegedCommandPrefix) -> {
                runCommand(event)
            }
        }
    }

    private fun runCommand(event: MessageReceivedEvent) {
        if (event.author.id == event.jda.selfUser.id || event.author.isBot) {
            logger.info("Not running command as author is me or a bot")
            return
        }

        if (!commandRegistry.isAuthorized(CommandPermission.BOT_ADMIN, event.author.id)) {
            event.channel.sendMessage("You are not an admin!").queue()
            return
        }

        val cmd =
            event.message.contentStripped
                .split(" ")[0]
                .removePrefix(properties.privilegedCommandPrefix)
        val command = commandRegistry.adminCommand(cmd)
        command.execute(event)
    }
}
