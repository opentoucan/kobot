package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class DisconnectCommand : Command {
    override fun execute(event: GuildMessageReceivedEvent) {
        if (event.channel.guild.audioManager.isConnected && !event.channel.guild.audioManager.isAttemptingToConnect) {
            event.channel.guild.audioManager.closeAudioConnection()
        }
        else{
            event.channel.sendMessage("I am not connected to an audio channel").queue()
        }
    }
}