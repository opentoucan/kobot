package uk.me.danielharman.kotlinspringbot.command.voice

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IVoiceCommand
import uk.me.danielharman.kotlinspringbot.provider.GuildMusicPlayerProvider
import java.awt.Color

@Component
class TrackInfoCommand(private val guildMusicPlayerProvider: GuildMusicPlayerProvider) : IVoiceCommand {

    private val commandString = "trackinfo"
    private val description = "Get the currently playing track"

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) {
        val guildAudioPlayer = guildMusicPlayerProvider.getGuildAudioPlayer(event.channel.guild)

        if (guildAudioPlayer.player.playingTrack == null) {
            event.channel.sendMessage(
                EmbedBuilder().setTitle("Error").setColor(Color.red)
                    .setDescription("Not playing anything").build()
            ).queue()
            return
        }
        val audioTrack = guildAudioPlayer.player.playingTrack

        val trackDuration = Period(audioTrack.duration)
        val playedDuration = Period(audioTrack.position)

        val fmt = PeriodFormatterBuilder()
            .printZeroAlways()
            .minimumPrintedDigits(2)
            .appendHours()
            .appendSeparator(":")
            .printZeroAlways()
            .minimumPrintedDigits(2)
            .appendMinutes()
            .appendSeparator(":")
            .printZeroAlways()
            .minimumPrintedDigits(2)
            .appendSeconds()
            .toFormatter()

        val durationStr = "${fmt.print(playedDuration)}/${fmt.print(trackDuration)}"

        event.channel.sendMessage(
            EmbedBuilder()
                .setTitle("Music")
                .setColor(0x2e298f)
                .addField("Track Title", audioTrack.info.title, false)
                .addField("Track Author", audioTrack.info.author, false)
                .addField("Track Length", durationStr, false)
                .build()
        ).queue()
    }
}