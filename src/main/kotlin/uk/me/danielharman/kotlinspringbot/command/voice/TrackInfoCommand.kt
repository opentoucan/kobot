package uk.me.danielharman.kotlinspringbot.command.voice

import net.dv8tion.jda.api.EmbedBuilder
import org.apache.commons.lang3.time.DurationFormatUtils
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.provider.GuildMusicPlayerProvider
import java.awt.Color

@Component
class TrackInfoCommand(
    private val guildMusicPlayerProvider: GuildMusicPlayerProvider,
) : Command("trackinfo", "Get the currently playing track"),
    ISlashCommand {
    override fun execute(event: DiscordMessageEvent) {
        if (event.guild == null) {
            event.reply(Embeds.createErrorEmbed("This command can only be used in Servers"))
            return
        }

        val guildAudioPlayer = guildMusicPlayerProvider.getGuildAudioPlayer(event.guild)

        if (guildAudioPlayer.player.playingTrack == null) {
            event.reply(
                EmbedBuilder()
                    .setTitle("Error")
                    .setColor(Color.red)
                    .setDescription("Not playing anything")
                    .build(),
            )
            return
        }
        val audioTrack = guildAudioPlayer.player.playingTrack

        val fmt = "mm:ss"

        val durationStr =
            "${DurationFormatUtils.formatDuration(
                audioTrack.position,
                fmt,
            )}/${DurationFormatUtils.formatDuration(audioTrack.duration, fmt)}"

        event.reply(
            EmbedBuilder()
                .setTitle("Music")
                .setColor(0x2e298f)
                .addField("Track Title", audioTrack.info.title, false)
                .addField("Track Author", audioTrack.info.author, false)
                .addField("Track Length", durationStr, false)
                .build(),
        )
    }
}
