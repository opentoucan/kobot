package uk.me.danielharman.kotlinspringbot.command.admin

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand

class DefaultAdminCommand(private val msg: String): IAdminCommand {

    override fun execute(event: GuildMessageReceivedEvent) {
        event.channel.sendMessage("No such command $msg").queue()
    }

    //Never match we only want to use this explicitly
    override fun matchCommandString(str: String): Boolean = false
    override fun getCommandString(): String = ""
}