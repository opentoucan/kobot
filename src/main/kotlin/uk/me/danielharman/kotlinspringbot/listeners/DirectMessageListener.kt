package uk.me.danielharman.kotlinspringbot.listeners

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.factories.AdminCommandFactory

@Component
class DirectMessageListener(
    private val commandFactory: AdminCommandFactory,
    private val properties: KotlinBotProperties
) : ListenerAdapter() {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
        when {
            event.message.contentStripped.startsWith(properties.privilegedCommandPrefix) -> {
                runCommand(event)
            }
        }
    }

    private fun runCommand(event: PrivateMessageReceivedEvent) {

        if (event.author.id == event.jda.selfUser.id || event.author.isBot) {
            logger.info("Not running command as author is me or a bot")
            return
        }

        event.channel

        val cmd = event.message.contentStripped.split(" ")[0].removePrefix(properties.privilegedCommandPrefix)
        val command = commandFactory.getCommand(cmd)
        command.execute(event)
    }

}