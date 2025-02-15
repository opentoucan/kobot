package uk.me.danielharman.kotlinspringbot.command.moderators

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IModeratorCommand

private const val CAREFUL_NOW_LIMIT = 50

@Component
class PurgeMessagesCommand : IModeratorCommand {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val commandString: String = "purge"

    override fun matchCommandString(str: String): Boolean = commandString == str

    override fun getCommandString(): String = commandString

    override fun execute(event: MessageReceivedEvent) {
        val s = event.message.contentStripped.split(" ")

        if (s.size < 2) {
            event.channel.sendMessage("Number of to delete messages not given.").queue()
            return
        }

        val number = s[1].toInt()

        if (number > CAREFUL_NOW_LIMIT) {
            event.channel.sendMessage("Careful now!").queue()
            return
        }

        val messages =
            event.channel.history
                .retrievePast(number)
                .complete()

        try {
            event.channel.purgeMessages(messages)
        } catch (e: InsufficientPermissionException) {
            logger.error(e.message, e)
            event.channel.sendMessage("I don't have permissions to delete messages!").queue()
            return
        }

        event.channel
            .sendMessage(
                "https://cdn.discordapp.com/attachments/554379034750877707/650988065539620874/giphy_1.gif",
            ).queue()
    }
}
