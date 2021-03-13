package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.ICommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService

@Component
class SearchCommand(private val commandService: DiscordCommandService) : ICommand {

    private val commandString = "search"
    private val description = "Search for commands"

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) {

        val split = event.message.contentStripped.split(' ')

        if (split.size <= 1) {
            event.channel.sendMessage(Embeds.createErrorEmbed("No search term given.")).queue()
            return
        }

        val searchCommand = commandService.searchCommand(event.guild.id, split[1])

        val builder = EmbedBuilder()
                .setTitle("Matched commands")
                .setColor(0x9d03fc)

        if (searchCommand.isEmpty()) {
            event.channel.sendMessage(builder.setDescription("No matching commands found.").build()).queue()
            return
        }

        builder.appendDescription("Command - Closeness to ${split[1]}\n\n")
        searchCommand.forEach { cmd ->
            builder.appendDescription("${cmd.first} - ${cmd.second}%\n")
        }

        event.channel.sendMessage(builder.build()).queue()
    }

}