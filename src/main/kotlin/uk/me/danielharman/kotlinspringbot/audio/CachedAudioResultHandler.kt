package uk.me.danielharman.kotlinspringbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioTrack
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import uk.me.danielharman.kotlinspringbot.services.CachedAudio

/**
 * Loads a file downloaded by yt-dlp, replacing the track details lavaplayer reads from the file
 * (which usually has no title) with the video's details, then hands it to [delegate].
 */
class CachedAudioResultHandler(
    private val audio: CachedAudio,
    private val delegate: AudioLoadResultHandler,
) : AudioLoadResultHandler {
    override fun trackLoaded(track: AudioTrack) {
        delegate.trackLoaded(withVideoDetails(track))
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        delegate.playlistLoaded(playlist)
    }

    override fun noMatches() {
        delegate.noMatches()
    }

    override fun loadFailed(exception: FriendlyException) {
        delegate.loadFailed(exception)
    }

    private fun withVideoDetails(track: AudioTrack): AudioTrack {
        if (track !is LocalAudioTrack) return track

        val info =
            AudioTrackInfo(
                audio.title,
                audio.uploader,
                track.duration,
                track.identifier,
                false,
                audio.url,
            )
        return LocalAudioTrack(info, track.containerTrackFactory, track.sourceManager as LocalAudioSourceManager)
    }
}
