package uk.me.danielharman.kotlinspringbot.command.moderators

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IModeratorCommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds

@Component
class ListModeratorCommandsCommand(private val commands: List<IModeratorCommand>) : IModeratorCommand {

    private val commandString = "commands"

    override fun execute(event: GuildMessageReceivedEvent) {
        val stringBuilder = StringBuilder()

        for (command in commands) {
            stringBuilder.append("${command.getCommandString()}\n")
        }
        event.channel.sendMessage(Embeds.infoEmbedBuilder().appendDescription(stringBuilder.toString()).build()).queue()
    }

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString

}