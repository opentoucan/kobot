package uk.me.danielharman.kotlinspringbot.provider

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import net.dv8tion.jda.api.entities.Guild
import uk.me.danielharman.kotlinspringbot.audio.GuildMusicManager

class GuildMusicPlayerProvider {
    val playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    private val musicManagers: HashMap<Long, GuildMusicManager> = hashMapOf()

    init {
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)
    }
    @Synchronized
    fun getGuildAudioPlayer(guild: Guild): GuildMusicManager {
        val guildId = guild.idLong
        var musicManager = musicManagers[guildId]

        if (musicManager == null) {
            musicManager = GuildMusicManager(playerManager)
            musicManagers[guildId] = musicManager
        }

        guild.audioManager.sendingHandler = musicManager.getSendHandler()
        return musicManager
    }
}