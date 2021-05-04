package uk.me.danielharman.kotlinspringbot.command.voice

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IVoiceCommand
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

@Component
class GetVolumeCommand(private val springGuildService: SpringGuildService) : IVoiceCommand {

    private val commandString = listOf("getvol", "getvolume")
    private val description = "Get the bot's current volume level"

    override fun matchCommandString(str: String): Boolean = commandString.contains(str)

    override fun getCommandString(): String = commandString.joinToString(", ")

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) =
        event.channel.sendMessage("${springGuildService.getVol(event.guild.id)}").queue()
}