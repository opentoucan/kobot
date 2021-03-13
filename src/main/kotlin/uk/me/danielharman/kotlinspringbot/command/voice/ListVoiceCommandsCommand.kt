package uk.me.danielharman.kotlinspringbot.command.voice

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IVoiceCommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds

@Component
class ListVoiceCommandsCommand(private val commands: List<IVoiceCommand>) : IVoiceCommand {

    private val commandString = "listcommands"
    private val description = "Get the list of inbuilt voice commands"

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
            Embeds.infoEmbedBuilder().appendDescription(stringBuilder.toString()).setTitle("Voice Commands").build()
        ).queue()
    }

}