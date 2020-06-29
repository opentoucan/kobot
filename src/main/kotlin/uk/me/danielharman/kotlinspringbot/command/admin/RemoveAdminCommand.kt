package uk.me.danielharman.kotlinspringbot.command.admin

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.command.Command
import uk.me.danielharman.kotlinspringbot.services.GuildService

class RemoveAdminCommand(val guildService: GuildService) : Command {
    override fun execute(event: GuildMessageReceivedEvent) {

        event.message.mentionedUsers.forEach { u -> guildService.removedPrivileged(event.guild.id, u.id) }
        event.channel.sendMessage("Removed ${event.message.mentionedUsers}").queue()

    }
}