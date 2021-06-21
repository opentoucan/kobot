package uk.me.danielharman.kotlinspringbot.command

import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.Param
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.messages.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService

@Component
class DeleteCommand(private val commandService: DiscordCommandService) : Command(
    "deletecommand",
    "Delete a custom command",
    listOf(Param(0, "Name", Param.ParamType.Text, "Command name to delete", true))
) {

    override fun execute(event: DiscordMessageEvent) {
        val split = event.content.split(" ")

        if (split.size < 2) {
            event.channel.sendMessage(Embeds.createErrorEmbed("Command not found")).queue()
            return
        }

        when (commandService.deleteCommand(event.guild?.id ?: "", split[1])) {
            is Failure -> event.channel.sendMessage(Embeds.createErrorEmbed("Command not found")).queue()
            is Success -> event.channel.sendMessage(Embeds.infoEmbedBuilder().setDescription("Command deleted").build())
                .queue()
        }
    }
}