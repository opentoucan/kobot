package uk.me.danielharman.kotlinspringbot.command.voice

import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.services.DiscordActionService

@Component
class VoiceMoveCommand(
    private val discordActionService: DiscordActionService,
) : Command("voicemove", "Enable mass moving of members to different voice channels"),
    ISlashCommand {
    override fun execute(event: DiscordMessageEvent) {
        if (event.guild == null) {
            event.reply(Embeds.createErrorEmbed("This command can only be used in Servers"))
            return
        }

        val guild = event.guild
        val audioManager = guild.audioManager
        val member = guild.getMember(event.author)

        if (member == null) {
            event.reply("Could not find member.")
            return
        }
        val voiceState = member.voiceState

        if (voiceState == null) {
            event.reply("You don't seem to be in a channel.")
            return
        }

        val voiceChannel = voiceState.channel

        if (voiceChannel == null) {
            event.reply("You don't seem to be in a channel.")
            return
        }

        try {
            if (!audioManager.isConnected) {
                audioManager.openAudioConnection(voiceChannel)
            }
        } catch (e: InsufficientPermissionException) {
            logger.error(
                "Bot encountered an exception when attempting to join a voice channel ${e.message}",
            )
            event.reply("I don't have permission to join.")
            return
        }

        discordActionService.enableVoiceMove()
        event.reply("Voice move enabled!")
    }
}
