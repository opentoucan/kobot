package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.ApplicationLogger

class DisconnectCommand : Command {
    override fun execute(event: GuildMessageReceivedEvent) {
        event.guild.audioManager.openAudioConnection(event.member?.voiceState?.channel)
        if (event.guild.audioManager.isConnected || event.guild.audioManager.queuedAudioConnection != null) {
            event.channel.guild.audioManager.closeAudioConnection()
        }
        else{
            event.channel.sendMessage("I am not connected to an audio channel").queue()
        }
    }
}