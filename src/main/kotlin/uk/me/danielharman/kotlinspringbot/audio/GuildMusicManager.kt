package uk.me.danielharman.kotlinspringbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import uk.me.danielharman.kotlinspringbot.services.DiscordActionService
import java.util.concurrent.ConcurrentHashMap

class GuildMusicManager(
    manager: AudioPlayerManager,
    guildId: String,
    discordService: DiscordActionService,
) : AudioEventListener {

    var player: AudioPlayer = manager.createPlayer()
    var scheduler: TrackScheduler = TrackScheduler(player, guildId, discordService)

    private val callbacks: MutableMap<AudioTrack, (String) -> Unit> = ConcurrentHashMap()

    init {
        player.addListener(scheduler)
        player.addListener(this)
    }

    fun getSendHandler(): AudioPlayerSendHandler = AudioPlayerSendHandler(player)

    fun registerCallback(track: AudioTrack, func: (String) -> Unit) {
        callbacks[track] = func
    }

    override fun onEvent(event: AudioEvent) {
        when (event) {
            is TrackExceptionEvent -> {
                callbacks.remove(event.track)?.let {
                    it(
                        "An error occurred when trying to play the track," +
                            " the track may be age restricted or have embedding disabled.",
                    )
                }
            }

            is TrackEndEvent -> {
                callbacks.remove(event.track)
            }
        }
    }
}
