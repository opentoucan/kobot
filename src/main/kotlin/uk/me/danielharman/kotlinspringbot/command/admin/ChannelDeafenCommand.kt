package uk.me.danielharman.kotlinspringbot.command.admin

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand
import uk.me.danielharman.kotlinspringbot.services.GuildService

@Component
class ChannelDeafenCommand(private val guildService: GuildService) : IAdminCommand {

    private val commandString: String = "deafen"

    override fun matchCommandString(str: String): Boolean = commandString == str

    override fun getCommandString(): String = commandString

    override fun execute(event: GuildMessageReceivedEvent) {
        val silenceChannel = guildService.deafenChannel(event.guild.id, event.channel.id)
        if (silenceChannel){
            event.channel.sendMessage("Channel has been deafened. Use 'undeafened' command to undo.").queue()
        }
        else {
            event.channel.sendMessage("Failed to deafen channel.").queue()
        }
    }
}