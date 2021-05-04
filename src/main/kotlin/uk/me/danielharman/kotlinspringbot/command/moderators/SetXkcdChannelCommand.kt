package uk.me.danielharman.kotlinspringbot.command.moderators

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IModeratorCommand
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

@Component
class SetXkcdChannelCommand(private val springGuildService: SpringGuildService) : IModeratorCommand {

    private val commandString: String = "setxkcdchannel"

    override fun matchCommandString(str: String): Boolean = commandString == str

    override fun getCommandString(): String = commandString

    override fun execute(event: GuildMessageReceivedEvent) {
        springGuildService.setXkcdChannel(event.guild.id, event.channel.id)
        event.channel.sendMessage("Set as xkcd channel").queue()
    }
}