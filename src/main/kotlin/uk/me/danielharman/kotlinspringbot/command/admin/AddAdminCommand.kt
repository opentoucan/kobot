package uk.me.danielharman.kotlinspringbot.command.admin

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.command.Command
import uk.me.danielharman.kotlinspringbot.services.GuildService

class AddAdminCommand(private val guildService: GuildService) : Command {
    override fun execute(event: GuildMessageReceivedEvent) {

        val mentionedUsers = event.message.mentionedUsers

        if(mentionedUsers.size <= 0)
        {
            event.channel.sendMessage("No users were provided").queue()
            return
        }
        
        mentionedUsers.forEach { u ->
                guildService.addPrivileged(event.guild.id, u.id)
                event.channel.sendMessage("Added ${u.asTag}").queue()
        }
    }
}