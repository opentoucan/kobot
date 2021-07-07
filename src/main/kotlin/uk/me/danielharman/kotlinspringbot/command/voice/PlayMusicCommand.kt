package uk.me.danielharman.kotlinspringbot.command.voice

import net.dv8tion.jda.api.entities.VoiceChannel
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.audio.NewAudioResultHandler
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import uk.me.danielharman.kotlinspringbot.models.CommandParameter.ParamType
import uk.me.danielharman.kotlinspringbot.provider.GuildMusicPlayerProvider
import uk.me.danielharman.kotlinspringbot.services.DiscordActionService
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

@Component
class PlayMusicCommand(
    private val guildMusicPlayerProvider: GuildMusicPlayerProvider,
    private val springGuildService: SpringGuildService,
    private val discordActionService: DiscordActionService
) : Command(
    "play", "Play audio via Youtube, Vimeo etc.",
    listOf(CommandParameter(0, "url", ParamType.Word, "Url to play music from"))
), ISlashCommand {

    override fun execute(event: DiscordMessageEvent) {
        val voiceChannel: VoiceChannel?

        if (event.guild == null) {
            return
        }

        val paramValue = event.getParamValue(this.commandParameters[0])
        val url = paramValue.asString()

        if (url == null || paramValue.error) {
            val player = guildMusicPlayerProvider.getGuildAudioPlayer(event.guild).player
            player.isPaused = !player.isPaused

            val message = if (player.isPaused) "Paused" else "Playing"
            event.reply(message)
            return
        }

        val member = event.guild.retrieveMember(event.author).complete()

        if (member == null) {
            event.reply("Can't find member!!!!")
            return
        }

        val voiceState = member.voiceState

        if (voiceState == null) {
            event.reply("Can't find member voicestate! Are you in a channel?")
            return
        }

        voiceChannel = voiceState.channel

        if (voiceChannel == null) {
            event.reply("Can't find voice channel! Are you in a channel?")
            return
        }
        when (val botVoiceChannel = discordActionService.getBotVoiceChannel(event.guild.id)) {
            is Success -> {
                logger.info("Bot's voice channel: " + botVoiceChannel.value.id)
                event.guild.audioManager.openAudioConnection(botVoiceChannel.value)
            }
        }

        if (voiceChannel != event.guild.audioManager.connectedChannel) {
            logger.info("My voice channel: " + voiceChannel.id)
            event.guild.audioManager.closeAudioConnection()
            event.guild.audioManager.openAudioConnection(voiceChannel)
        }

        logger.info("Connected voice channel from manager: " + event.guild.audioManager.connectedChannel?.id)
        val musicManager = guildMusicPlayerProvider.getGuildAudioPlayer(voiceChannel.guild)
        guildMusicPlayerProvider.playerManager.loadItemOrdered(
            musicManager,
            url,
            NewAudioResultHandler(voiceChannel, musicManager, event, springGuildService, event.guild)
        )
    }
}