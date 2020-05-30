package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class PingCommand : Command {
    override fun execute(event: GuildMessageReceivedEvent) {
        return event.channel.sendMessage("pong ${event.author.asMention}").queue()
    }
}