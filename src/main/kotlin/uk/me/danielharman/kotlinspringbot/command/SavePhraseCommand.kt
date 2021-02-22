package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Embeds.infoEmbedBuilder
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService

class SavePhraseCommand(private val commandService: DiscordCommandService) : Command {

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