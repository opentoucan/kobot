package uk.me.danielharman.kotlinspringbot.command.moderators

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IModeratorCommand
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

@Component
class IsChannelDeafenedCommand(private val springGuildService: SpringGuildService) : IModeratorCommand {

    private val commandString: String = "isdeafened"

    override fun matchCommandString(str: String): Boolean = commandString == str

    override fun getCommandString(): String = commandString

    override fun execute(event: MessageReceivedEvent) {

        val message = when(val deafenedChannels = springGuildService.getDeafenedChannels(event.guild.id)){
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