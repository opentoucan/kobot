package uk.me.danielharman.kotlinspringbot.command

import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Embeds.infoEmbedBuilder
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService

@Component
class SavePhraseCommand(private val commandService: DiscordCommandService) : Command(
    "save", "Save a custom command", listOf(
        CommandParameter(0, "Name", CommandParameter.ParamType.Word, "Command name", true),
        CommandParameter(1, "Content", CommandParameter.ParamType.String, "Text content only", true)
    )
), ISlashCommand {

    override fun execute(event: DiscordMessageEvent) {

        val nameParam = event.getParamValue(commandParameters[0])
        val contentParam = event.getParamValue(commandParameters[1])

        val content = contentParam.asString()
        val name = nameParam.asString()

        val attachments = event.attachments

        if (nameParam.error || name == null) {
            event.reply(Embeds.createErrorEmbed("Command name not given"))
            return
        }

        if ((contentParam.error || content == null) && attachments.isEmpty()) {
            event.reply(Embeds.createErrorEmbed("Content missing"))
            return
        }

        if (attachments.isNotEmpty()) {
            val attachment = attachments[0]
            when (val result = commandService.createFileCommand(
                event.guild?.id ?: "",
                name,
                attachment.fileName,
                event.author.id,
                attachment.retrieveInputStream().get()
            )) {
                is Failure -> event.reply(Embeds.createErrorEmbed(result.reason))
                is Success -> event.reply(
                    infoEmbedBuilder().setDescription("Saved command as ${result.value.key}").build()
                )
            }
        } else {
            if (name.contains(Regex("[_.!,?$\\\\-]"))) {
                event.reply(Embeds.createErrorEmbed("Cannot save with that phrase"))
                return
            }

            if (content == null) return

            when (val result = commandService.createStringCommand(
                event.guild?.id ?: "", name,
                content, event.author.id, true
            )) {
                is Failure -> event.reply(Embeds.createErrorEmbed(result.reason))
                is Success -> event.reply(
                    infoEmbedBuilder().setDescription("Saved command as ${result.value.key}").build()
                )
            }
        }
    }

}