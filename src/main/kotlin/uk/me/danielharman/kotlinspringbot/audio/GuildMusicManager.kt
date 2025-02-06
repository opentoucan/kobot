package uk.me.danielharman.kotlinspringbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent
import uk.me.danielharman.kotlinspringbot.services.DiscordActionService

class GuildMusicManager(manager: AudioPlayerManager, guildId: String, discordService: DiscordActionService) : AudioEventListener {

    var player: AudioPlayer = manager.createPlayer()
    var scheduler: TrackScheduler = TrackScheduler(player, guildId, discordService)

    private val callbacks: HashMap<String, (String) -> Unit> = HashMap()

    init {
        player.addListener(scheduler)
        player.addListener(this)
    }

    fun getSendHandler(): AudioPlayerSendHandler = AudioPlayerSendHandler(player)

    fun registerCallback(trackIdentifier: String, func: (String) -> Unit) {
        callbacks[trackIdentifier] = func
    }

    override fun onEvent(event: AudioEvent) {
        when (event) {
            is TrackExceptionEvent -> {
                callbacks[event.track.identifier]?.let { it("An error occurred when trying to play the track, the track may be age restricted or have embedding disabled.") }
            }
            is TrackEndEvent -> {
                callbacks.remove(event.track.identifier)
            }
        }
    }
}
