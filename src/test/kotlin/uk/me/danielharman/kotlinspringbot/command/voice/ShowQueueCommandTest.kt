package uk.me.danielharman.kotlinspringbot.command.voice

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.managers.AudioManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import uk.me.danielharman.kotlinspringbot.audio.GuildMusicManager
import uk.me.danielharman.kotlinspringbot.audio.TrackScheduler
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import uk.me.danielharman.kotlinspringbot.models.ParsedCommandParameter
import uk.me.danielharman.kotlinspringbot.provider.GuildMusicPlayerProvider
import java.io.InputStream
import java.util.concurrent.LinkedBlockingQueue

internal class ShowQueueCommandTest {
    @Test
    fun `queue is displayed when there is no currently playing track`() {
        val provider = mock(GuildMusicPlayerProvider::class.java)
        val command = ShowQueueCommand(provider)
        val guild = mock(Guild::class.java)
        var repliedEmbed: MessageEmbed? = null
        val event =
            object : DiscordMessageEvent(
                "",
                mock(MessageChannel::class.java),
                mock(User::class.java),
                guild,
            ) {
                override fun reply(msg: String, invokerOnly: Boolean) = Unit

                override fun reply(embed: MessageEmbed, invokerOnly: Boolean) {
                    repliedEmbed = embed
                }

                override fun reply(file: InputStream, filename: String) = Unit

                override fun getParamValue(commandParameter: CommandParameter): ParsedCommandParameter = throw UnsupportedOperationException()
            }
        val audioManager = mock(AudioManager::class.java)
        val musicManager = mock(GuildMusicManager::class.java)
        val scheduler = mock(TrackScheduler::class.java)
        val player = mock(AudioPlayer::class.java)
        val track = mock(AudioTrack::class.java)
        val trackInfo = AudioTrackInfo("Queued track", "Artist", 60_000L, "track-id", false, "https://example.com")
        val queue = LinkedBlockingQueue<Pair<AudioTrack, String>>()
        queue.offer(track to "channel")

        `when`(guild.audioManager).thenReturn(audioManager)
        `when`(provider.getGuildAudioPlayer(guild)).thenReturn(musicManager)
        `when`(musicManager.scheduler).thenReturn(scheduler)
        `when`(musicManager.player).thenReturn(player)
        `when`(scheduler.queue).thenReturn(queue)
        `when`(player.playingTrack).thenReturn(null)
        `when`(track.info).thenReturn(trackInfo)
        `when`(track.duration).thenReturn(60_000L)

        command.execute(event)

        val description = repliedEmbed?.description.orEmpty()
        assertTrue(description.contains("Up Next: Queued track"))
        assertTrue(!description.contains("Now Playing:"))
    }
}
