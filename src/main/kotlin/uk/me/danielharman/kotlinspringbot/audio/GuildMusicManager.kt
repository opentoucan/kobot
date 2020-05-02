package uk.me.danielharman.kotlinspringbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager

class GuildMusicManager(var manager: AudioPlayerManager) {

    var player: AudioPlayer = manager.createPlayer()
    var scheduler: TrackScheduler

    init {
        scheduler = TrackScheduler(player)
        player.addListener(scheduler)
    }

    fun getSendHandler(): AudioPlayerSendHandler {
        return AudioPlayerSendHandler(player)
    }

}