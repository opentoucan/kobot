package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.ICommand

@Component
class PingCommand : ICommand {
    override fun execute(event: GuildMessageReceivedEvent) = event.channel.sendMessage("pong ${event.author.asMention}").queue()
    override fun matchCommandString(str: String): Boolean = str.toLowerCase() == "ping"
    override fun getCommandString(): String = "ping"
    override fun getCommandDescription(): String = "pong"
}