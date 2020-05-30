package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.provider.GuildMusicPlayerProvider
import uk.me.danielharman.kotlinspringbot.services.GuildService

class SetVolumeCommand(private val guildMusicPlayerProvider: GuildMusicPlayerProvider, private val guildService: GuildService): Command {
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