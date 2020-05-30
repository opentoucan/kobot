package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.provider.GuildMusicPlayerProvider

class SkipTrackCommand(private val guildMusicPlayerProvider: GuildMusicPlayerProvider): Command {
    override fun execute(event: GuildMessageReceivedEvent) {
        val musicManager = guildMusicPlayerProvider.getGuildAudioPlayer(event.channel.guild)
        musicManager.scheduler.nextTrack()
        event.channel.sendMessage("Skipped to next track.").queue()
    }
}