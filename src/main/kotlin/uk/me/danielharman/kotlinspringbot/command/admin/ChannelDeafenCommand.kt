package uk.me.danielharman.kotlinspringbot.command.admin

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.command.Command
import uk.me.danielharman.kotlinspringbot.services.GuildService

class ChannelDeafenCommand(private val guildService: GuildService) : Command {
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