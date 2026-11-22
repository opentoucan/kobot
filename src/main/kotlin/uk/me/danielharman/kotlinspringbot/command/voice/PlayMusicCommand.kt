package uk.me.danielharman.kotlinspringbot.command.voice

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.audio.CachedAudioResultHandler
import uk.me.danielharman.kotlinspringbot.audio.GuildMusicManager
import uk.me.danielharman.kotlinspringbot.audio.NewAudioResultHandler
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import uk.me.danielharman.kotlinspringbot.models.CommandParameter.ParamType
import uk.me.danielharman.kotlinspringbot.provider.GuildMusicPlayerProvider
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService
import uk.me.danielharman.kotlinspringbot.services.YoutubeAudioService

@Component
class PlayMusicCommand(
    private val guildMusicPlayerProvider: GuildMusicPlayerProvider,
    private val springGuildService: SpringGuildService,
    private val youtubeAudioService: YoutubeAudioService,
) : Command(
    "play",
    "Play YouTube (url or search terms), Vimeo, SoundCloud etc. Pauses or resumes with no input",
    listOf(CommandParameter(0, "query", ParamType.String, "YouTube search terms, or a url to play")),
),
    ISlashCommand {

    override fun execute(event: DiscordMessageEvent) {
        val guild = event.guild

        if (guild == null) {
            event.reply(Embeds.createErrorEmbed("This command can only be used in Servers"))
            return
        }

        val paramValue = event.getParamValue(commandParameters[0])
        val query = paramValue.asString()

        if (query.isNullOrBlank() || paramValue.error) {
            val player = guildMusicPlayerProvider.getGuildAudioPlayer(guild).player
            player.isPaused = !player.isPaused
            event.reply(if (player.isPaused) "Paused" else "Playing")
            return
        }

        val voiceChannel =
            guild
                .retrieveMember(event.author)
                .complete()
                ?.voiceState
                ?.channel
                ?.takeIf { it.type == ChannelType.VOICE }
                ?.asVoiceChannel()

        if (voiceChannel == null) {
            event.reply(Embeds.createErrorEmbed("You need to be in a voice channel"))
            return
        }

        val musicManager = guildMusicPlayerProvider.getGuildAudioPlayer(guild)
        val handler = NewAudioResultHandler(voiceChannel, musicManager, event, springGuildService, guild)

        if (youtubeAudioService.isYoutubeQuery(query)) {
            playFromYoutube(query, event, guild, voiceChannel, musicManager, handler)
        } else {
            // Other sites are streamed by lavaplayer
            if (joinVoiceChannel(event, guild, voiceChannel)) {
                guildMusicPlayerProvider.playerManager.loadItemOrdered(musicManager, query.trim(), handler)
            }
        }
    }

    private fun playFromYoutube(
        query: String,
        event: DiscordMessageEvent,
        guild: Guild,
        voiceChannel: VoiceChannel,
        musicManager: GuildMusicManager,
        handler: AudioLoadResultHandler,
    ) {
        val audio =
            when (val result = youtubeAudioService.fetchAudio(query)) {
                is Failure -> {
                    event.reply(Embeds.createErrorEmbed("Could not play: ${result.reason}"))
                    return
                }

                is Success -> result.value
            }

        if (!joinVoiceChannel(event, guild, voiceChannel)) return

        logger.info("Playing ${audio.videoId} from ${if (audio.fromCache) "cache" else "fresh download"}")
        guildMusicPlayerProvider.playerManager.loadItemOrdered(
            musicManager,
            audio.file.toString(),
            CachedAudioResultHandler(audio, handler),
        )
    }

    private fun joinVoiceChannel(
        event: DiscordMessageEvent,
        guild: Guild,
        voiceChannel: VoiceChannel,
    ): Boolean {
        if (guild.audioManager.connectedChannel == voiceChannel) return true

        return try {
            guild.audioManager.openAudioConnection(voiceChannel)
            true
        } catch (e: InsufficientPermissionException) {
            logger.error("Bot encountered an exception when attempting to join a voice channel ${e.message}")
            event.reply(Embeds.createErrorEmbed("I don't have permission to join ${voiceChannel.name}"))
            false
        }
    }
}
