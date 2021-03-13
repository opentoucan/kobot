package uk.me.danielharman.kotlinspringbot.command.voice

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IVoiceCommand
import uk.me.danielharman.kotlinspringbot.provider.GuildMusicPlayerProvider
import uk.me.danielharman.kotlinspringbot.services.GuildService

@Component
class SetVolumeCommand(
    private val guildMusicPlayerProvider: GuildMusicPlayerProvider,
    private val guildService: GuildService
) : IVoiceCommand {

    private val commandString = listOf("vol", "volume")
    private val description = "Set the bot's volume level (0-100)"

    override fun matchCommandString(str: String): Boolean = commandString.contains(str)

    override fun getCommandString(): String = commandString.joinToString(", ")

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) {
        val vol = event.message.contentStripped.split(" ")[1].toInt()
        val musicManager = guildMusicPlayerProvider.getGuildAudioPlayer(event.channel.guild)

        val newVol = when {
            vol > 100 -> 100
            vol < 0 -> 0
            else -> vol
        }
        musicManager.player.volume = newVol
        guildService.setVol(event.channel.guild.id, newVol)
        event.channel.sendMessage("Setting volume to $newVol").queue()
    }
}