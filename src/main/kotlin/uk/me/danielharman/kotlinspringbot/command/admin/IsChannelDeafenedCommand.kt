package uk.me.danielharman.kotlinspringbot.command.admin

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.command.Command
import uk.me.danielharman.kotlinspringbot.services.GuildService

class IsChannelDeafenedCommand(private val guildService: GuildService) : Command {
    override fun execute(event: GuildMessageReceivedEvent) {

        val deafenedChannels = guildService.getDeafenedChannels(event.guild.id)

        if(deafenedChannels.contains(event.channel.id))
            event.channel.sendMessage("Channel is currently deafened.").queue()
        else
            event.channel.sendMessage("Channel is not currently deafened.").queue()

    }
}