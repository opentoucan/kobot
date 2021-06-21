package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.Param
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.messages.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService

@Component
class SearchCommand(private val commandService: DiscordCommandService) : Command(
    "search",
    "Search for commands",
    listOf(Param(0, "SearchText", Param.ParamType.Text, "Text to search for", true))
) {

    override fun execute(event: DiscordMessageEvent) {

        val split = event.content.split(' ')

        if (split.size <= 1) {
            event.reply(Embeds.createErrorEmbed("No search term given."))
            return
        }

        val builder = EmbedBuilder()
            .setTitle("Matched commands")
            .setColor(0x9d03fc)

        when (val searchCommand = commandService.searchCommand(event.guild?.id ?: "", split[1])) {
            is Failure -> builder.setDescription("No matching commands found.")
            is Success -> {
                builder.appendDescription("Command - Closeness to ${split[1]}\n\n")
                searchCommand.value.forEach { cmd ->
                    builder.appendDescription("${cmd.first} - ${cmd.second}%\n")
                }
            }
        }

        event.reply(builder.build())
    }

}