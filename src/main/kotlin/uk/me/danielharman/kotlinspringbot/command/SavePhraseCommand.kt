package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.Param
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Embeds.infoEmbedBuilder
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.messages.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService

@Component
class SavePhraseCommand(private val commandService: DiscordCommandService) : Command(
    "save", "Save a custom command", listOf(
        Param(0, "Name", Param.ParamType.Text, "Command name", true),
        Param(1, "Content", Param.ParamType.Text, "Text content only", true)
    )
) {

    override fun execute(event: DiscordMessageEvent)  {

        val channel = event.channel
        val content = event.content
        val split = content.split(" ")
        val attachments = event.attachments

        if (split.size < 3 && attachments.isEmpty()) {
            event.reply(Embeds.createErrorEmbed("Content missing"))
            return
        }

        if (attachments.isNotEmpty()) {
            val attachment = attachments[0]
            when (val result = commandService.createFileCommand(
                event.guild?.id ?: "",
                split[1],
                attachment.fileName,
                event.author.id,
                attachment.retrieveInputStream().get()
            )) {
                is Failure -> channel.sendMessage(Embeds.createErrorEmbed(result.reason)).queue()
                is Success -> channel.sendMessage(
                    infoEmbedBuilder().setDescription("Saved command as ${result.value.key}").build()
                ).queue()
            }
        } else {
            if (split[1].contains(Regex("[_.!,?$\\\\-]"))) {
                channel.sendMessage(Embeds.createErrorEmbed("Cannot save with that phrase")).queue()
                return
            }
            when (val result = commandService.createStringCommand(
                event.guild?.id ?: "", split[1],
                split.subList(2, split.size).joinToString(" "), event.author.id, true
            )) {
                is Failure -> channel.sendMessage(Embeds.createErrorEmbed(result.reason)).queue()
                is Success -> channel.sendMessage(
                    infoEmbedBuilder().setDescription("Saved command as ${result.value.key}").build()
                ).queue()
            }
        }
    }

}