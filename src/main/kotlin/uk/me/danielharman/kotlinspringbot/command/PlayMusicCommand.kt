package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.audio.NewAudioResultHandler
import uk.me.danielharman.kotlinspringbot.provider.GuildMusicPlayerProvider
import uk.me.danielharman.kotlinspringbot.services.GuildService

class PlayMusicCommand(private val guildMusicPlayerProvider: GuildMusicPlayerProvider, private val guildService: GuildService): Command {

    override fun execute(event: GuildMessageReceivedEvent) {
        val split = event.message.contentStripped.split(" ")
        if (split.size < 2) {
            val player = guildMusicPlayerProvider.getGuildAudioPlayer(event.guild).player
            player.isPaused = !player.isPaused

            val message = if (player.isPaused) "Paused" else "Playing"
            event.channel.sendMessage(message).queue()
            return
        }

        val musicManager = guildMusicPlayerProvider.getGuildAudioPlayer(event.channel.guild)

        val member = event.member

        if (member == null) {
            event.channel.sendMessage("Can't find member!!!!").queue()
            return
        }

        val voiceState = member.voiceState

        if (voiceState == null) {
            event.channel.sendMessage("Can't find member voicestate! Are you in a channel?").queue()
            return
        }

        val voiceChannel = voiceState.channel

        if (voiceChannel == null) {
            event.channel.sendMessage("Can't find voice channel! Are you in a channel?").queue()
            return
        }
        event.guild.audioManager.openAudioConnection(event.member?.voiceState?.channel)
        guildMusicPlayerProvider.playerManager.loadItemOrdered(musicManager, split[1], NewAudioResultHandler(voiceChannel, musicManager, event.channel, guildService))
    }
}