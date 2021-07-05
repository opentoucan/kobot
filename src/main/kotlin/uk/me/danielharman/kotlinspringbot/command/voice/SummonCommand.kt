package uk.me.danielharman.kotlinspringbot.command.voice

import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.helpers.Embeds

@Component
class SummonCommand : Command("summon", "Make the bot join the voice channel"), ISlashCommand {

    override fun execute(event: DiscordMessageEvent) {

        if (event.guild == null) {
            event.reply(Embeds.createErrorEmbed("Could not find guild"))
            return
        }

        val audioManager = event.guild.audioManager
        val member = event.guild.retrieveMember(event.author).complete()

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
            audioManager.openAudioConnection(voiceChannel)
        } catch (e: InsufficientPermissionException) {
            logger.error("Bot encountered an exception when attempting to join a voice channel ${e.message}")
            event.reply("I don't have permission to join.")
        }
    }

}