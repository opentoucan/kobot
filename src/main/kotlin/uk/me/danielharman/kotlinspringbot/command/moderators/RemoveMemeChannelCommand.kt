package uk.me.danielharman.kotlinspringbot.command.moderators

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IModeratorCommand
import uk.me.danielharman.kotlinspringbot.services.GuildService

@Component
class RemoveMemeChannelCommand(private val guildService: GuildService) : IModeratorCommand {

    private val commandString: String = "removememechannel"

    override fun matchCommandString(str: String): Boolean = commandString == str

    override fun getCommandString(): String = commandString

    override fun execute(event: GuildMessageReceivedEvent) {
        guildService.removeMemeChannel(event.guild.id, event.channel.id)
        event.channel.sendMessage("Removed meme channel").queue()
    }
}