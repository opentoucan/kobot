package uk.me.danielharman.kotlinspringbot.command.voice

import net.dv8tion.jda.api.EmbedBuilder
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.HelperFunctions.formatDurationString
import uk.me.danielharman.kotlinspringbot.helpers.PURPLE
import uk.me.danielharman.kotlinspringbot.provider.GuildMusicPlayerProvider

@Component
class ShowQueueCommand(private val guildMusicPlayerProvider: GuildMusicPlayerProvider) :
    Command("queue", "Get the list of queued tracks"),
    ISlashCommand {
    override fun execute(event: DiscordMessageEvent) {
        if (event.guild == null) {
            event.reply(Embeds.createErrorEmbed("This command can only be used in Servers"))
            return
        }

        val guildAudioPlayer = guildMusicPlayerProvider.getGuildAudioPlayer(event.guild)

        val queue = guildAudioPlayer.scheduler.queue.toList()

        val playingTrack = guildAudioPlayer.player.playingTrack

        if (queue.isEmpty() && playingTrack == null) {
            event.reply("Queue is empty")
            return
        }

        val stringBuilder = StringBuilder()

        stringBuilder.append(
            "Now Playing: ${playingTrack.info.title} " +
                "${playingTrack.position.formatDurationString()}/${playingTrack.duration.formatDurationString()}\n",
        )

        if (queue.isNotEmpty()) {
            val upNext = queue.first().first
            stringBuilder.append("Up Next: ${upNext.info.title} ${upNext.duration.formatDurationString()}\n")
            for (i in 1..<queue.size) {
                val track = queue[i].first
                stringBuilder.append("${i + 1}: ${track.info.title} ${track.duration.formatDurationString()}\n")
            }
        }

        event.reply(
            EmbedBuilder()
                .appendDescription(stringBuilder.toString())
                .setColor(PURPLE)
                .setTitle("Queued Tracks")
                .build(),
        )
    }
}
