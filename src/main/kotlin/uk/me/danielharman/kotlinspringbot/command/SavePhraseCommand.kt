package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.ICommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Embeds.infoEmbedBuilder
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService

@Component
class SavePhraseCommand(private val commandService: DiscordCommandService) : ICommand {

    private val commandString = listOf("save", "set")
    private val description = "Save a new command"

    override fun matchCommandString(str: String): Boolean = commandString.contains(str)

    override fun getCommandString(): String = commandString.joinToString(", ")

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) {

        val content = event.message.contentRaw
        val split = content.split(" ")

        if (split.size < 3 && event.message.attachments.size <= 0) {
            event.message.channel.sendMessage(Embeds.createErrorEmbed("Content missing")).queue()
            return
        }

        if (event.message.attachments.size > 0) {
            val attachment = event.message.attachments[0]
            commandService.createFileCommand(event.message.guild.id, split[1], attachment.fileName, event.author.id, attachment.retrieveInputStream().get())
        } else {
            if (split[1].contains(Regex("[_.!,?$\\\\-]"))) {
                event.message.channel.sendMessage(Embeds.createErrorEmbed("Cannot save with that phrase")).queue()
                return
            }
            commandService.createStringCommand(event.message.guild.id, split[1], split.subList(2, split.size).joinToString(" "), event.author.id, true)
        }
        event.message.channel.sendMessage(infoEmbedBuilder().setDescription("Saved command as ${split[1]}").build()).queue()
    }

}