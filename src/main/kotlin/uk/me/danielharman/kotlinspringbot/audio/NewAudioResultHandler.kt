package uk.me.danielharman.kotlinspringbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.HelperFunctions.partialWrapper
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

//TODO re-write because dumb things because java inline class overrides
class NewAudioResultHandler(
    private val voiceChannel: VoiceChannel?, private val musicManager: GuildMusicManager,
    private val event: DiscordMessageEvent, private val springGuildService: SpringGuildService, private val guild: Guild
) : AudioLoadResultHandler {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun trackLoaded(track: AudioTrack) {
        play(track)
        event.reply("Queued track")
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        var firstTrack = playlist.selectedTrack

        if (firstTrack == null) {
            firstTrack = playlist.tracks[0]
        }

        event.reply("Adding to queue ${firstTrack.info.title} (first track of playlist ${playlist.name}")
        play(firstTrack)

    }

    override fun noMatches() {
        event.reply("Nothing found for that url")
    }

    override fun loadFailed(exception: FriendlyException) {
        event.reply("Could not play: ${exception.message}")
    }

    fun play(track: AudioTrack) {
        if (voiceChannel == null)
            return

        musicManager.registerCallback (track.identifier, partialWrapper(::onErrorEvent, event))

        if (!guild.audioManager.isConnected && !guild.audioManager.isAttemptingToConnect) {
            try {
                guild.audioManager.openAudioConnection(voiceChannel)
            } catch (e: InsufficientPermissionException) {
                logger.error("Bot encountered an exception when attempting to join a voice channel ${e.message}")
            }
        }
       when(val vol = springGuildService.getVol(guild.id)){
           is Failure -> logger.error("Failed to get guild volume ${vol.reason}")
           is Success -> musicManager.player.volume = vol.value
       }
        musicManager.scheduler.queue(track)

    }

    private fun onErrorEvent(event: DiscordMessageEvent, message: String){
        event.reply(Embeds.createErrorEmbed(message), false)
    }

}