package uk.me.danielharman.kotlinspringbot.command.interfaces

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

interface IVoiceCommand {
    fun execute(event: GuildMessageReceivedEvent)
    fun matchCommandString(str: String): Boolean
    fun getCommandString(): String
    fun getCommandDescription(): String
}