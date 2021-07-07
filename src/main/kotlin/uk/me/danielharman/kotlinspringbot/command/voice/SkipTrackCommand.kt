package uk.me.danielharman.kotlinspringbot.command.voice

import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.provider.GuildMusicPlayerProvider

@Component
class SkipTrackCommand(private val guildMusicPlayerProvider: GuildMusicPlayerProvider) :
    Command("skip", "Skip the currently playing track"), ISlashCommand {

    override fun execute(event: DiscordMessageEvent) {
        if (event.guild == null) {
            event.reply("Could not find guild")
            return
        }

        val musicManager = guildMusicPlayerProvider.getGuildAudioPlayer(event.guild)
        musicManager.scheduler.nextTrack()
        event.reply("Skipped to next track.")
    }
}