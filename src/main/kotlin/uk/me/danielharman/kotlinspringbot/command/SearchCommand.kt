package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService

@Component
class SearchCommand(private val commandService: DiscordCommandService) : Command(
    "search",
    "Search for commands",
    listOf(CommandParameter(0, "SearchText", CommandParameter.ParamType.Word, "Text to search for", true))
), ISlashCommand {

    override fun execute(event: DiscordMessageEvent) {

        val paramValue = event.getParamValue(commandParameters[0])
        val searchTerm = paramValue.asString()

        if (paramValue.error || searchTerm == null) {
            event.reply(Embeds.createErrorEmbed("No search term given."))
            return
        }

        val builder = EmbedBuilder()
            .setTitle("Matched commands")
            .setColor(0x9d03fc)

        when (val searchCommand = commandService.searchCommand(event.guild?.id ?: "", searchTerm)) {
            is Failure -> builder.setDescription("No matching commands found.")
            is Success -> {
                builder.appendDescription("Command - Closeness to $searchTerm\n\n")
                searchCommand.value.forEach { cmd ->
                    builder.appendDescription("${cmd.first} - ${cmd.second}%\n")
                }
            }
        }

        event.reply(builder.build())
    }

}