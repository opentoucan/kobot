package uk.me.danielharman.kotlinspringbot.command.moderators

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IModeratorCommand
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

@Component
class RemoveMemeChannelCommand(private val springGuildService: SpringGuildService) : IModeratorCommand {

    private val commandString: String = "removememechannel"

    override fun matchCommandString(str: String): Boolean = commandString == str

    override fun getCommandString(): String = commandString

    override fun execute(event: MessageReceivedEvent) {
        springGuildService.removeMemeChannel(event.guild.id, event.channel.id)
        event.channel.sendMessage("Removed meme channel").queue()
    }
}