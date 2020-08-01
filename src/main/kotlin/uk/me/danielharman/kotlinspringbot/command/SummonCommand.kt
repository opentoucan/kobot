package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import uk.me.danielharman.kotlinspringbot.ApplicationLogger

class SummonCommand : VoiceCommand {
    override var voiceChannel: VoiceChannel? = null
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

        try {
            audioManager.openAudioConnection(voiceChannel)
        } catch (e: InsufficientPermissionException) {
            ApplicationLogger.logger.error("Bot encountered an exception when attempting to join a voice channel ${e.message}")
            event.channel.sendMessage("I don't have permission to join.").queue()
        }
    }

}