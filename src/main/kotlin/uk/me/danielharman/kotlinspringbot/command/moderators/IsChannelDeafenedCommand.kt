package uk.me.danielharman.kotlinspringbot.command.moderators

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IModeratorCommand
import uk.me.danielharman.kotlinspringbot.services.GuildService

@Component
class IsChannelDeafenedCommand(private val guildService: GuildService) : IModeratorCommand {

    private val commandString: String = "isdeafened"

    override fun matchCommandString(str: String): Boolean = commandString == str

    override fun getCommandString(): String = commandString

    override fun execute(event: GuildMessageReceivedEvent) {

        val deafenedChannels = guildService.getDeafenedChannels(event.guild.id)

        if(deafenedChannels.contains(event.channel.id))
            event.channel.sendMessage("Channel is currently deafened.").queue()
        else
            event.channel.sendMessage("Channel is not currently deafened.").queue()

    }
}