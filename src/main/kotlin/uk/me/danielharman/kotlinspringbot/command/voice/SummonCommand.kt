package uk.me.danielharman.kotlinspringbot.command.voice

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IVoiceCommand

@Component
class SummonCommand : IVoiceCommand {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val commandString = "summon"
    private val description = "Make the bot join the voice channel"

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

        try {
            audioManager.openAudioConnection(voiceChannel)
        } catch (e: InsufficientPermissionException) {
            logger.error("Bot encountered an exception when attempting to join a voice channel ${e.message}")
            event.channel.sendMessage("I don't have permission to join.").queue()
        }
    }

}