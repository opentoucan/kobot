package uk.me.danielharman.kotlinspringbot.command.moderators

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IModeratorCommand
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.services.GuildService

@Component
class ChannelUnDeafenCommand(private val guildService: GuildService) : IModeratorCommand {

    private val commandString: String = "undeafen"

    override fun matchCommandString(str: String): Boolean = commandString == str

    override fun getCommandString(): String = commandString

    override fun execute(event: GuildMessageReceivedEvent) {
        val message = when (guildService.unDeafenChannel(event.guild.id, event.channel.id)) {
            is Failure -> "Failed to undeafen channel."
            is Success -> "Channel has been undeafened."
        }
        event.channel.sendMessage(message).queue()
    }
}