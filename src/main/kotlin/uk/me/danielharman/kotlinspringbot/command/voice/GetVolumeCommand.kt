package uk.me.danielharman.kotlinspringbot.command.voice

import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

@Component
class GetVolumeCommand(private val springGuildService: SpringGuildService) :
    Command("getvol", "Get the bot's current volume level"), ISlashCommand {

    override fun execute(event: DiscordMessageEvent) {
        val guild = event.guild

        if (guild == null) {
            event.reply(Embeds.createErrorEmbed("This command can only be used in Servers"))
            return
        }

        when (val vol = springGuildService.getVol(event.guild?.id ?: "")) {
            is Failure -> event.reply(Embeds.createErrorEmbed("Could not get volume for guild"))
            is Success -> event.reply("Bot volume is ${vol.value}")
        }
    }
}