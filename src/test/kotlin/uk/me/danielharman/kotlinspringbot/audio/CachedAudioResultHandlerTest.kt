package uk.me.danielharman.kotlinspringbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioTrack
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import uk.me.danielharman.kotlinspringbot.services.CachedAudio
import java.io.ByteArrayInputStream
import java.nio.file.Path
import java.time.Duration
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

internal class CachedAudioResultHandlerTest {
    @TempDir
    private lateinit var directory: Path

    private val playerManager = DefaultAudioPlayerManager().also { AudioSourceManagers.registerLocalSource(it) }

    @AfterEach
    fun tearDown() {
        playerManager.shutdown()
    }

    private class RecordingHandler : AudioLoadResultHandler {
        var loadedTrack: AudioTrack? = null

        override fun trackLoaded(track: AudioTrack) {
            loadedTrack = track
        }

        override fun playlistLoaded(playlist: AudioPlaylist) = error("Unexpected playlist")

        override fun noMatches() = error("Unexpected no matches")

        override fun loadFailed(exception: FriendlyException) = throw exception
    }

    // One second of silence, which lavaplayer can load without any other tooling
    private fun writeWav(): Path {
        val format = AudioFormat(48_000f, 16, 2, true, false)
        val data = ByteArray(format.frameSize * format.frameRate.toInt())
        val file = directory.resolve("jNQXAC9IVRw.wav")
        AudioSystem.write(
            AudioInputStream(ByteArrayInputStream(data), format, format.frameRate.toLong()),
            AudioFileFormat.Type.WAVE,
            file.toFile(),
        )
        return file
    }

    @Test
    fun shouldReplaceFileDetailsWithVideoDetails() {
        val file = writeWav()
        val audio =
            CachedAudio(
                file = file,
                videoId = "jNQXAC9IVRw",
                title = "Me at the zoo",
                uploader = "jawed",
                duration = Duration.ofSeconds(1),
                url = "https://www.youtube.com/watch?v=jNQXAC9IVRw",
                fromCache = false,
            )
        val delegate = RecordingHandler()

        playerManager.loadItem(file.toString(), CachedAudioResultHandler(audio, delegate)).get()

        val track = delegate.loadedTrack
        assertThat(track, notNullValue())
        assertThat(track, instanceOf(LocalAudioTrack::class.java))
        assertThat(track!!.info.title, equalTo("Me at the zoo"))
        assertThat(track.info.author, equalTo("jawed"))
        assertThat(track.info.uri, equalTo("https://www.youtube.com/watch?v=jNQXAC9IVRw"))
        assertThat(track.identifier, equalTo(file.toString()))
        assertThat(track.duration, equalTo(1000L))
    }
}
