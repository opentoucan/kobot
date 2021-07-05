package uk.me.danielharman.kotlinspringbot.command

import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService

@Component
class DeleteCommand(private val commandService: DiscordCommandService) : Command(
    "deletecommand",
    "Delete a custom command",
    listOf(CommandParameter(0, "Name", CommandParameter.ParamType.Word, "Command name to delete", true))
) {

    override fun execute(event: DiscordMessageEvent) {

        val paramValue = event.getParamValue(commandParameters[0])

        if (paramValue.error || paramValue.value == null) {
            event.reply(Embeds.createErrorEmbed("Invalid command name"))
            return
        }

        when (commandService.deleteCommand(event.guild?.id ?: "", paramValue.asString() ?: "")) {
            is Failure -> event.reply(Embeds.createErrorEmbed("Command not found"))
            is Success -> event.reply(Embeds.infoEmbedBuilder().setDescription("Command deleted").build())
        }
    }
}