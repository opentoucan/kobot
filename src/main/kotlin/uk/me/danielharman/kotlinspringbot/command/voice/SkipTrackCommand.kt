package uk.me.danielharman.kotlinspringbot.command.voice

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IVoiceCommand
import uk.me.danielharman.kotlinspringbot.provider.GuildMusicPlayerProvider

@Component
class SkipTrackCommand(private val guildMusicPlayerProvider: GuildMusicPlayerProvider) : IVoiceCommand {

    private val commandString = "skip"
    private val description = "Skip the currently playing track"

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) {
        val musicManager = guildMusicPlayerProvider.getGuildAudioPlayer(event.channel.guild)
        musicManager.scheduler.nextTrack()
        event.channel.sendMessage("Skipped to next track.").queue()
    }
}