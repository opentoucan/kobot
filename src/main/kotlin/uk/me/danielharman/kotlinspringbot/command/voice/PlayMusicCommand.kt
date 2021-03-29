package uk.me.danielharman.kotlinspringbot.command.voice

import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.audio.NewAudioResultHandler
import uk.me.danielharman.kotlinspringbot.command.interfaces.IVoiceCommand
import uk.me.danielharman.kotlinspringbot.helpers.JDAHelperFunctions.getBotVoiceChannel
import uk.me.danielharman.kotlinspringbot.provider.GuildMusicPlayerProvider
import uk.me.danielharman.kotlinspringbot.services.GuildService

@Component
class PlayMusicCommand(
    private val guildMusicPlayerProvider: GuildMusicPlayerProvider,
    private val guildService: GuildService
) : IVoiceCommand {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val commandString = "play"
    private val description = "Play audio via Youtube, Vimeo etc."

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) {
        val voiceChannel: VoiceChannel?

        val split = event.message.contentStripped.split(" ")
        if (split.size < 2) {
            val player = guildMusicPlayerProvider.getGuildAudioPlayer(event.guild).player
            player.isPaused = !player.isPaused

            val message = if (player.isPaused) "Paused" else "Playing"
            event.channel.sendMessage(message).queue()
            return
        }

        val member = event.member

        if (member == null) {
            event.channel.sendMessage("Can't find member!!!!").queue()
            return
        }

        val voiceState = member.voiceState

        if (voiceState == null) {
            event.channel.sendMessage("Can't find member voicestate! Are you in a channel?").queue()
            return
        }

        voiceChannel = voiceState.channel

        if (voiceChannel == null) {
            event.channel.sendMessage("Can't find voice channel! Are you in a channel?").queue()
            return
        }
        val botVoiceChannel = getBotVoiceChannel(event)
        if (botVoiceChannel != null) {
            logger.info("Bot's voice channel: " + botVoiceChannel?.id)
            event.guild.audioManager.openAudioConnection(botVoiceChannel)
        }
        if (voiceChannel != event.guild.audioManager.connectedChannel) {
            logger.info("My voice channel: " + voiceChannel?.id)
            event.guild.audioManager.closeAudioConnection()
            event.guild.audioManager.openAudioConnection(voiceChannel)
        }
        logger.info("Connected voice channel from manager: " + event.guild.audioManager.connectedChannel?.id)
        val musicManager = guildMusicPlayerProvider.getGuildAudioPlayer(voiceChannel!!.guild)
        guildMusicPlayerProvider.playerManager.loadItemOrdered(
            musicManager,
            split[1],
            NewAudioResultHandler(voiceChannel, musicManager, event.channel, guildService)
        )
    }
}