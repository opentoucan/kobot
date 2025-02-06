package uk.me.danielharman.kotlinspringbot.command.moderators

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IModeratorCommand

@Component
class SetNickCommand : IModeratorCommand {
    private val commandString: String = "setnick"

    override fun matchCommandString(str: String): Boolean = commandString == str

    override fun getCommandString(): String = commandString

    override fun execute(event: MessageReceivedEvent) {
        val split = event.message.contentStripped.split('"')

        val mentionedMembers = event.message.mentions.members

        for (member in mentionedMembers) {
            try {
                member.modifyNickname(split[1]).queue()
                event.channel.sendMessage("Set member's nickname!").queue()
            } catch (e: Exception) {
                event.channel.sendMessage("Could not change ${member.nickname}'s nickname!").queue()
            }
        }
    }
}
