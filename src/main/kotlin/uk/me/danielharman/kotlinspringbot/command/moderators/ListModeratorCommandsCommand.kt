package uk.me.danielharman.kotlinspringbot.command.moderators

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.command.interfaces.IModeratorCommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds

@Component
class ListModeratorCommandsCommand(
    private val commands: List<IModeratorCommand>,
    private val properties: KotlinBotProperties
) : IModeratorCommand {

    private val commandString = "commands"

    override fun execute(event: GuildMessageReceivedEvent) {
        val stringBuilder = StringBuilder()

        commands.sortedBy { c -> c.getCommandString() }.forEach { c ->
            run {
                stringBuilder.append("${properties.privilegedCommandPrefix}${c.getCommandString()}\n")
            }
        }

        event.channel.sendMessage(
            Embeds.infoEmbedBuilder().appendDescription(stringBuilder.toString()).setTitle("Moderator Commands").build()
        ).queue()
    }

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString

}