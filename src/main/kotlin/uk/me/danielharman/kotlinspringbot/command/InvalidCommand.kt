package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class InvalidCommand: Command {
    override fun execute(event: GuildMessageReceivedEvent) {
        event.channel.sendMessage("Invalid command").queue()
    }
}