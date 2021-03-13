package uk.me.danielharman.kotlinspringbot.command.admin

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand
import uk.me.danielharman.kotlinspringbot.services.GuildService

@Component
class RemoveMemeChannelCommand(private val guildService: GuildService) : IAdminCommand {

    private val commandString: String = "removememechannel"

    override fun matchCommandString(str: String): Boolean = commandString == str

    override fun getCommandString(): String = commandString

    override fun execute(event: GuildMessageReceivedEvent) {
        guildService.removeMemeChannel(event.guild.id, event.channel.id)
        event.channel.sendMessage("Removed meme channel").queue()
    }
}