package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.ICommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds

@Component
class ListCommandsCommand(private val commands: List<ICommand>) : ICommand {

    private val commandString = "listcommands"
    private val description = "Get the list of inbuilt commands"

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) {

        val stringBuilder = StringBuilder()

        commands.sortedBy { c -> c.getCommandString() }.forEach { c ->
            run {
                stringBuilder.append("${c.getCommandString()}: ${c.getCommandDescription()}\n")
            }
        }

        event.channel.sendMessage(
            Embeds.infoEmbedBuilder().appendDescription(stringBuilder.toString()).setTitle("Commands").build()
        ).queue()
    }

}