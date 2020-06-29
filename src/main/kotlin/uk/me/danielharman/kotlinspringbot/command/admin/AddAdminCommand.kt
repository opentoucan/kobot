package uk.me.danielharman.kotlinspringbot.command.admin

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.command.Command
import uk.me.danielharman.kotlinspringbot.services.GuildService

class AddAdminCommand(val guildService: GuildService) : Command {
    override fun execute(event: GuildMessageReceivedEvent) {
        event.message.mentionedUsers.forEach { u -> guildService.addPrivileged(event.guild.id, u.id) }
        event.channel.sendMessage("Added ${event.message.mentionedUsers}").queue()
    }
}