package uk.me.danielharman.kotlinspringbot.command.voice

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IVoiceCommand

@Component
class VoiceMoveCommand : IVoiceCommand {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val commandString = "voicemove"
    private val description = "Enable mass moving of members to different voice channels"

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString

    override fun getCommandDescription(): String = description

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

        if (!audioManager.isConnected) {
            audioManager.openAudioConnection(voiceChannel)
        }

        try {
            event.message.channel.sendMessage("Voice move enabled!").queue()
            event.jda.addEventListener(MoveListener())
        } catch (e: InsufficientPermissionException) {
            logger.error("Bot encountered an exception when attempting to join a voice channel ${e.message}")
            event.channel.sendMessage("I don't have permission to join.").queue()
        }

    }

    class MoveListener : ListenerAdapter() {
        override fun onGuildVoiceMove(event: GuildVoiceMoveEvent) {
            if (event.jda.selfUser.id == event.member.id) {
                event.channelLeft.members.forEach { m -> m.guild.moveVoiceMember(m, event.channelJoined).queue() }
                event.jda.removeEventListener(this)
                event.guild.audioManager.closeAudioConnection()
            }
        }
    }
}






