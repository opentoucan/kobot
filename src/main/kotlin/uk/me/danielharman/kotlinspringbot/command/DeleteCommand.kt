package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.ICommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService

@Component
class DeleteCommand(private val commandService: DiscordCommandService) : ICommand {

    private val commandString = "deletecommand"
    private val description = "Delete a custom command"

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) {

        val content = event.message.contentRaw
        val split = content.split(" ")

        if (split.size < 2) {
            event.channel.sendMessage(Embeds.createErrorEmbed("Command not found")).queue()
            return
        }

        if (commandService.deleteCommand(event.guild.id, split[1])) {
            event.channel.sendMessage(Embeds.infoEmbedBuilder().setDescription("Command deleted").build()).queue()
        } else {
            event.channel.sendMessage(Embeds.createErrorEmbed("Command not found")).queue()
        }

    }
}