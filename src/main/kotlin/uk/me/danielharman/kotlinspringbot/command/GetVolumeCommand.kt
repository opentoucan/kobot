package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.services.GuildService

class GetVolumeCommand(private val guildService: GuildService): Command {
    override fun execute(event: GuildMessageReceivedEvent) =
            event.channel.sendMessage("${guildService.getVol(event.guild.id)}").queue()
}