package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.hooks.EventListener
import uk.me.danielharman.kotlinspringbot.ApplicationLogger

class VoiceMoveCommand: Command {

    override fun execute(event: GuildMessageReceivedEvent) {
        if (!event.channel.guild.audioManager.isConnected && !event.channel.guild.audioManager.isAttemptingToConnect) {
            try {
                event.channel.guild.audioManager.openAudioConnection(event.member?.voiceState?.channel)
            } catch (e: InsufficientPermissionException) {
                ApplicationLogger.logger.error("Bot encountered an exception when attempting to join a voice channel ${e.message}")
            }
        }
        event.message.channel.sendMessage("Voice move enabled!").queue()
        event.jda.addEventListener(MoveListener())
    }

    class MoveListener : EventListener {
        override fun onEvent(event: GenericEvent) {
            if (event is GuildVoiceMoveEvent) {
                event.channelLeft.members.forEach { m -> m.guild.moveVoiceMember(m, event.channelJoined).queue() }
                event.jda.removeEventListener(this)
            }
        }
    }
}






