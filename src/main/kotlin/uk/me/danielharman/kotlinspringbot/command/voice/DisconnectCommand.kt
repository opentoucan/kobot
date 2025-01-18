package uk.me.danielharman.kotlinspringbot.command.voice

import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.services.DiscordActionService

@Component
class DisconnectCommand(private val discordActionService: DiscordActionService) :
    Command("disconnect", "Disconnect the bot from the current voice channel"), ISlashCommand {

    override fun execute(event: DiscordMessageEvent) {
        val guild = event.guild

        if (guild == null) {
            event.reply(Embeds.createErrorEmbed("This command can only be used in Servers"))
            return
        }

        when (val channel = discordActionService.getBotVoiceChannel(event.guild.id)) {
            is Success -> event.guild.audioManager.openAudioConnection(channel.value)
            is Failure<*> -> TODO()
        }

        if (guild.audioManager.isConnected || event.guild.audioManager.queuedAudioConnection != null) {
            guild.audioManager.closeAudioConnection()
        } else {
            event.reply("I am not connected to an audio channel")
        }

    }
}