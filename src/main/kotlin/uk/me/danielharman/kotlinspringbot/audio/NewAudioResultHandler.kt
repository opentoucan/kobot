package uk.me.danielharman.kotlinspringbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

//TODO re-write because dumb things because java inline class overrides
class NewAudioResultHandler(
    private val voiceChannel: VoiceChannel?, private val musicManager: GuildMusicManager,
    private val channel: MessageChannel, private val springGuildService: SpringGuildService, private val guild: Guild
) : AudioLoadResultHandler {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun trackLoaded(track: AudioTrack) {
        play(track)
        channel.sendMessage("Queued track").queue()
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        var firstTrack = playlist.selectedTrack

        if (firstTrack == null) {
            firstTrack = playlist.tracks[0]
        }

        channel.sendMessage("Adding to queue ${firstTrack.info.title} (first track of playlist ${playlist.name}")
        play(firstTrack)

    }

    override fun noMatches() {
        channel.sendMessage("Nothing found by").queue()
    }

    override fun loadFailed(exception: FriendlyException) {
        channel.sendMessage("Could not play: ${exception.message}").queue()
    }

    fun play(track: AudioTrack) {
        if (voiceChannel == null)
            return

        if (!guild.audioManager.isConnected && !guild.audioManager.isAttemptingToConnect) {
            try {
                guild.audioManager.openAudioConnection(voiceChannel)
            } catch (e: InsufficientPermissionException) {
                logger.error("Bot encountered an exception when attempting to join a voice channel ${e.message}")
            }
        }
        val vol = springGuildService.getVol(guild.id)
        musicManager.scheduler.queue(track)
        musicManager.player.volume = (vol as Success).value
    }
}