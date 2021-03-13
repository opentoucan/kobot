package uk.me.danielharman.kotlinspringbot.command.admin

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand
import uk.me.danielharman.kotlinspringbot.services.GuildService

class AddMemeChannelCommand(private val guildService: GuildService) : IAdminCommand {

    private val commandString: String = "addmemechannel"

    override fun execute(event: GuildMessageReceivedEvent) {
        guildService.addMemeChannel(event.guild.id, event.channel.id)
        event.channel.sendMessage("Added meme channel").queue()
    }

    override fun matchCommandString(str: String): Boolean = commandString == str

    override fun getCommandString(): String = commandString

}