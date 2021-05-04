package uk.me.danielharman.kotlinspringbot.command.moderators

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IModeratorCommand
import uk.me.danielharman.kotlinspringbot.services.GuildService

@Component
class AddModeratorCommand(private val guildService: GuildService) : IModeratorCommand {

    private val commandString: String = "addmoderator"

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
                guildService.addModerator(event.guild.id, u.id)
                event.channel.sendMessage("Added ${u.asTag}").queue()
        }
    }

}