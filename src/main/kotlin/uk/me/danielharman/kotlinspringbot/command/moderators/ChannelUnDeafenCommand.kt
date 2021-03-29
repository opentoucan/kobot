package uk.me.danielharman.kotlinspringbot.command.moderators

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IModeratorCommand
import uk.me.danielharman.kotlinspringbot.services.GuildService

@Component
class ChannelUnDeafenCommand(private val guildService: GuildService) : IModeratorCommand {

    private val commandString: String = "undeafen"

    override fun matchCommandString(str: String): Boolean = commandString == str

    override fun getCommandString(): String = commandString

    override fun execute(event: GuildMessageReceivedEvent) {
        val silenceChannel = guildService.unDeafenChannel(event.guild.id, event.channel.id)
        if (silenceChannel){
            event.channel.sendMessage("Channel has been undeafened.").queue()
        }
        else {
            event.channel.sendMessage("Failed to undeafen channel.").queue()
        }
    }
}