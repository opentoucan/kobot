package uk.me.danielharman.kotlinspringbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.managers.AudioManager
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

internal class NewAudioResultHandlerTest {
    @Test
    fun `play does not queue track when connecting to voice channel fails`() {
        val guild = mock(Guild::class.java)
        val audioManager = mock(AudioManager::class.java)
        val voiceChannel = mock(VoiceChannel::class.java)
        val track = mock(AudioTrack::class.java)
        val trackInfo = AudioTrackInfo("Track", "Artist", 60_000L, "track-id", false, "https://example.com")
        val event = mock(DiscordMessageEvent::class.java)
        val channel = mock(MessageChannel::class.java)
        val musicManager = mock(GuildMusicManager::class.java)
        val player = mock(AudioPlayer::class.java)
        val scheduler = mock(TrackScheduler::class.java)
        val springGuildService = mock(SpringGuildService::class.java)

        `when`(guild.id).thenReturn("guild")
        `when`(guild.audioManager).thenReturn(audioManager)
        `when`(audioManager.isConnected).thenReturn(false)
        `when`(event.channel).thenReturn(channel)
        `when`(track.info).thenReturn(trackInfo)
        `when`(musicManager.player).thenReturn(player)
        `when`(musicManager.scheduler).thenReturn(scheduler)
        doThrow(mock(InsufficientPermissionException::class.java))
            .`when`(audioManager)
            .openAudioConnection(voiceChannel)

        val handler =
            NewAudioResultHandler(
                voiceChannel,
                musicManager,
                event,
                springGuildService,
                guild,
            )

        handler.trackLoaded(track)

        verify(event).reply("I don't have permission to join that voice channel.", false)
        verify(event, never()).reply("Queued Track", false)
        verifyNoInteractions(musicManager, scheduler, springGuildService)
    }
}
