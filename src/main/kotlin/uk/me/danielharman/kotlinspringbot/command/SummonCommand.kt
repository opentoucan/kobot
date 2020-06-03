package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import uk.me.danielharman.kotlinspringbot.ApplicationLogger

class SummonCommand : Command {
    override fun execute(event: GuildMessageReceivedEvent) {
        val audioManager = event.guild.audioManager
        val member = event.member

        if (member == null) {
            event.channel.sendMessage("Could not find member.").queue()
            return
        }

        val voiceState = member.voiceState

        if (voiceState == null) {
            event.channel.sendMessage("You don't seem to be in a channel.").queue()
            return
        }

        val voiceChannel = voiceState.channel

        if (voiceChannel == null) {
            event.channel.sendMessage("You don't seem to be in a channel.").queue()
            return
        }

        if (!audioManager.isConnected && !audioManager.isAttemptingToConnect) {
            try {
                audioManager.openAudioConnection(voiceChannel)
            } catch (e: InsufficientPermissionException) {
                ApplicationLogger.logger.error("Bot encountered an exception when attempting to join a voice channel ${e.message}")
                event.channel.sendMessage("I don't have permission to join.").queue()
            }
        } else if (audioManager.isConnected) {
            if (audioManager.connectedChannel?.id != voiceChannel.id) {
                try {
                    audioManager.openAudioConnection(voiceChannel)
                } catch (e: InsufficientPermissionException) {
                    ApplicationLogger.logger.error("Bot encountered an exception when attempting to join a voice channel ${e.message}")
                    event.channel.sendMessage("I don't have permission to join.").queue()
                }
            }
        }
    }


}