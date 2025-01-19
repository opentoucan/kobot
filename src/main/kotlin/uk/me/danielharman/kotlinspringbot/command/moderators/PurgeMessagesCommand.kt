package uk.me.danielharman.kotlinspringbot.command.moderators

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IModeratorCommand

@Component
class PurgeMessagesCommand : IModeratorCommand {

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

        if (number > 50) {
            event.channel.sendMessage("Careful now!").queue()
            return
        }

        val messages = event.channel.history.retrievePast(number).complete()

        try {
            event.channel.purgeMessages(messages)
        } catch (e: InsufficientPermissionException) {
            event.channel.sendMessage("I don't have permissions to delete messages!").queue()
            return
        }

        event.channel.sendMessage("https://cdn.discordapp.com/attachments/554379034750877707/650988065539620874/giphy_1.gif").queue()

    }

}