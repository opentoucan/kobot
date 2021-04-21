package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.command.interfaces.ICommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds

@Component
class ListCommandsCommand(private val commands: List<ICommand>, private val properties: KotlinBotProperties) : ICommand {

    private val commandString = listOf("help","listcommands")
    private val description = "Get the list of inbuilt commands"

    override fun matchCommandString(str: String): Boolean = commandString.contains(str)

    override fun getCommandString(): String = commandString.joinToString(", ")

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) {

        val stringBuilder = StringBuilder()

        commands.sortedBy { c -> c.getCommandString() }.forEach { c ->
            run {
                stringBuilder.append("${properties.commandPrefix}${c.getCommandString()}: ${c.getCommandDescription()}\n")
            }
        }

        event.channel.sendMessage(
            Embeds.infoEmbedBuilder()
                .appendDescription("Text commands: ${properties.commandPrefix}help\n Voice commands: ${properties.voiceCommandPrefix}help\n\n\n")
                .appendDescription(stringBuilder.toString())
                .setTitle("Text Commands").build()
        ).queue()
    }

}