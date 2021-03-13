package uk.me.danielharman.kotlinspringbot.command.voice

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IVoiceCommand
import uk.me.danielharman.kotlinspringbot.helpers.JDAHelperFunctions.getBotVoiceChannel

@Component
class DisconnectCommand : IVoiceCommand {

    private val commandString = "disconnect"
    private val description = "Disconnect the bot from the current voice channel"

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) {

        val voiceChannel = getBotVoiceChannel(event)
        if (voiceChannel != null) {
            event.guild.audioManager.openAudioConnection(voiceChannel)
        }

        if (event.guild.audioManager.isConnected || event.guild.audioManager.queuedAudioConnection != null) {
            event.channel.guild.audioManager.closeAudioConnection()
        } else {
            event.channel.sendMessage("I am not connected to an audio channel").queue()
        }
    }
}