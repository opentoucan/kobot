package uk.me.danielharman.kotlinspringbot.command.moderators

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.command.interfaces.IModeratorCommand

class DefaultModeratorCommand(private val msg: String): IModeratorCommand {

    override fun execute(event: MessageReceivedEvent) {
        event.channel.sendMessage("No such command $msg").queue()
    }

    //Never match we only want to use this explicitly
    override fun matchCommandString(str: String): Boolean = false
    override fun getCommandString(): String = ""
}