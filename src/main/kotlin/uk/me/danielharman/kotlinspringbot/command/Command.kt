package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

interface Command {
    fun execute(event: GuildMessageReceivedEvent)
}