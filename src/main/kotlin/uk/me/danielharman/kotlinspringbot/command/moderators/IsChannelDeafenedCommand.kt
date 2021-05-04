package uk.me.danielharman.kotlinspringbot.command.moderators

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IModeratorCommand
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.services.GuildService

@Component
class IsChannelDeafenedCommand(private val guildService: GuildService) : IModeratorCommand {

    private val commandString: String = "isdeafened"

    override fun matchCommandString(str: String): Boolean = commandString == str

    override fun getCommandString(): String = commandString

    override fun execute(event: GuildMessageReceivedEvent) {

        val message = when(val deafenedChannels = guildService.getDeafenedChannels(event.guild.id)){
            is Failure -> deafenedChannels.reason
            is Success -> {
                if(deafenedChannels.value.contains(event.channel.id))
                    "Channel is currently deafened."
                else
                    "Channel is not currently deafened."
            }

        }
       event.channel.sendMessage(message).queue()
    }
}