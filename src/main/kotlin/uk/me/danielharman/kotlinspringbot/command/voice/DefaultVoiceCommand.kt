package uk.me.danielharman.kotlinspringbot.command.voice

import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.command.interfaces.IVoiceCommand

class DefaultVoiceCommand(private val msg: String) : IVoiceCommand {

    override fun execute(event: GuildMessageReceivedEvent) {
        event.channel.sendMessage("No such command $msg").queue()
    }

    //Never match we only want to use this explicitly
    override fun matchCommandString(str: String): Boolean = false
    override fun getCommandString(): String = ""
    override fun getCommandDescription(): String = ""
}