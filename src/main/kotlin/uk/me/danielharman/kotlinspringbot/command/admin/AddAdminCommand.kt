package uk.me.danielharman.kotlinspringbot.command.admin

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand
import uk.me.danielharman.kotlinspringbot.services.GuildService

@Component
class AddAdminCommand(private val guildService: GuildService) : IAdminCommand {

    private val commandString: String = "addadmin"

    override fun matchCommandString(str: String): Boolean = commandString == str

    override fun getCommandString(): String = commandString

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