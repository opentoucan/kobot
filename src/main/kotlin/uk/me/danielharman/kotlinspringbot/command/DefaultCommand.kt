package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.services.GuildService

class DefaultCommand(private val guildService: GuildService, private val command: String): Command {
    override fun execute(event: GuildMessageReceivedEvent) {
        event.channel.sendMessage(guildService.getCommand(event.guild.id, command)).queue()
    }
}