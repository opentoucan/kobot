package uk.me.danielharman.kotlinspringbot.provider

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioTrack
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import net.dv8tion.jda.api.entities.Guild
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.audio.GuildMusicManager
import uk.me.danielharman.kotlinspringbot.services.AudioFileUsage
import uk.me.danielharman.kotlinspringbot.services.DiscordActionService
import java.nio.file.Path

@Component
class GuildMusicPlayerProvider(private val discordService: DiscordActionService) : AudioFileUsage {

    final val playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    private val musicManagers: HashMap<Long, GuildMusicManager> = hashMapOf()

    init {
        // YouTube is downloaded with yt-dlp (YoutubeAudioService) instead of being streamed
        AudioSourceManagers.registerRemoteSources(playerManager, YoutubeAudioSourceManager::class.java)
        // Plays the files yt-dlp downloads
        AudioSourceManagers.registerLocalSource(playerManager)
    }

    @Synchronized
    fun getGuildAudioPlayer(guild: Guild): GuildMusicManager {
        val guildId = guild.idLong
        var musicManager = musicManagers[guildId]

        if (musicManager == null) {
            musicManager = GuildMusicManager(playerManager, guild.id, discordService)
            musicManagers[guildId] = musicManager
        }

        guild.audioManager.sendingHandler = musicManager.getSendHandler()
        return musicManager
    }

    @Synchronized
    override fun filesInUse(): Set<Path> = musicManagers.values
        .flatMap { manager -> listOfNotNull(manager.player.playingTrack) + manager.scheduler.queue.map { it.first } }
        .filterIsInstance<LocalAudioTrack>()
        .map { Path.of(it.identifier).toAbsolutePath().normalize() }
        .toSet()
}
