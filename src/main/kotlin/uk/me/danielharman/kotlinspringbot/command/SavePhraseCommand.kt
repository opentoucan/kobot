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
        if (event.guild == null) {
            event.reply(Embeds.createErrorEmbed("This command can only be used in Servers"))
            return
        }

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

        if (commandService.getCommand(event.guild.id, name) is Success) {
            event.reply(Embeds.createErrorEmbed("$name already exists, delete the command first to overwrite."))
            return
        }
        if (attachments.isNotEmpty()) {
            val attachment = attachments[0]
            //Don't allow files greater than 8MB
            if(attachment.size < 8388608) {
                when (val result = commandService.createFileCommand(
                    event.guild.id,
                    name,
                    attachment.fileName,
                    event.author.id,
                    attachment.retrieveInputStream().get()
                )) {
                    is Failure -> event.reply(Embeds.createErrorEmbed(result.reason))
                    is Success -> event.reply(infoEmbedBuilder().setDescription("Saved command as ${result.value.key}").build())
                }
            }else{
                event.reply(Embeds.createErrorEmbed("Attachment must be less than 8MB in size"))
            }
        } else {
            if (name.contains(Regex("[_.!,?$\\\\-]"))) {
                event.reply(Embeds.createErrorEmbed("Cannot save with that phrase"))
                return
            }

            if (content == null) return

            when (val result = commandService.createStringCommand(
                event.guild.id, name,
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