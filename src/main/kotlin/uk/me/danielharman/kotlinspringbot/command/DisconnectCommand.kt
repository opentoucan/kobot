package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.helpers.JDAHelperFunctions.getBotVoiceChannel

class DisconnectCommand : VoiceCommand {
    override var voiceChannel: VoiceChannel? = null
    override fun execute(event: GuildMessageReceivedEvent) {

        voiceChannel = getBotVoiceChannel(event)
        if(voiceChannel != null) {
            event.guild.audioManager.openAudioConnection(voiceChannel)
        }

        if (event.guild.audioManager.isConnected || event.guild.audioManager.queuedAudioConnection != null) {
            event.channel.guild.audioManager.closeAudioConnection()
        }
        else{
            event.channel.sendMessage("I am not connected to an audio channel").queue()
        }
    }
}