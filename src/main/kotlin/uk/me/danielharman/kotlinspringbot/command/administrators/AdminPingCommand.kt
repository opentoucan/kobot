package uk.me.danielharman.kotlinspringbot.command.administrators

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand

@Component
class AdminPingCommand : IAdminCommand {
    override fun execute(event: PrivateMessageReceivedEvent) =
        event.channel.sendMessage("pong ${event.author.asMention}").queue()
    override fun matchCommandString(str: String): Boolean = str.toLowerCase() == "ping"
    override fun getCommandString(): String = "ping"
}