package uk.me.danielharman.kotlinspringbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.me.danielharman.kotlinspringbot.helpers.onFailure
import uk.me.danielharman.kotlinspringbot.helpers.onSuccess
import uk.me.danielharman.kotlinspringbot.services.DiscordActionService
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class TrackScheduler(
    private val player: AudioPlayer,
    val guildId: String,
    val discordService: DiscordActionService,
) : AudioEventAdapter() {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    var queue: BlockingQueue<Pair<AudioTrack, String>> = LinkedBlockingQueue()

    fun queue(track: AudioTrack, callbackChannel: MessageChannel) {
        if (!player.startTrack(track, true)) {
            queue.offer(Pair(track, callbackChannel.id))
        }
    }

    fun clearQueue() {
        queue.clear()
        player.stopTrack()
    }

    fun nextTrack(): Boolean {
        val trackMessageChannelPair = queue.poll()

        if (trackMessageChannelPair == null) {
            player.stopTrack()
            return false
        }

        discordService
            .getGuild(guildId)
            .onSuccess { guild ->
                val channel = guild.getTextChannelById(trackMessageChannelPair.second)
                channel?.sendMessage("Now playing ${trackMessageChannelPair.first.info.title}")?.queue()
            }.onFailure { reason ->
                logger.warn("Could not announce next track in guild $guildId: $reason")
            }
        player.startTrack(trackMessageChannelPair.first, false)
        return true
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            nextTrack()
        }
    }
}
