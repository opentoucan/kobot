package uk.me.danielharman.kotlinspringbot.command.admin

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.command.Command

class DefaultCommand(val msg: String): Command {
    override fun execute(event: GuildMessageReceivedEvent) {
        event.channel.sendMessage("No such command $msg").queue()
    }
}