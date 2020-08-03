package uk.me.danielharman.kotlinspringbot.command.admin

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.command.Command
import uk.me.danielharman.kotlinspringbot.services.GuildService

class ChannelUnDeafenCommand(private val guildService: GuildService) : Command {
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