package uk.me.danielharman.kotlinspringbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import uk.me.danielharman.kotlinspringbot.services.DiscordActionService

internal class GuildMusicManagerTest {
    @Test
    fun `callbacks are kept separately for tracks with the same identifier`() {
        val audioPlayerManager = mock(AudioPlayerManager::class.java)
        val audioPlayer = mock(AudioPlayer::class.java)
        `when`(audioPlayerManager.createPlayer()).thenReturn(audioPlayer)
        val manager = GuildMusicManager(audioPlayerManager, "guild", mock(DiscordActionService::class.java))

        val firstTrack = mock(AudioTrack::class.java)
        val secondTrack = mock(AudioTrack::class.java)
        `when`(firstTrack.identifier).thenReturn("same-identifier")
        `when`(secondTrack.identifier).thenReturn("same-identifier")

        var firstCallbackCalls = 0
        var secondCallbackCalls = 0
        manager.registerCallback(firstTrack) { firstCallbackCalls++ }
        manager.registerCallback(secondTrack) { secondCallbackCalls++ }

        val firstException =
            TrackExceptionEvent(
                audioPlayer,
                firstTrack,
                FriendlyException("test", FriendlyException.Severity.COMMON, null),
            )
        manager.onEvent(firstException)

        assertEquals(1, firstCallbackCalls)
        assertEquals(0, secondCallbackCalls)

        val secondException =
            TrackExceptionEvent(
                audioPlayer,
                secondTrack,
                FriendlyException("test", FriendlyException.Severity.COMMON, null),
            )
        manager.onEvent(secondException)

        assertEquals(1, firstCallbackCalls)
        assertEquals(1, secondCallbackCalls)

        manager.onEvent(firstException)
        assertEquals(1, firstCallbackCalls)
    }
}
