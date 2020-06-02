package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.models.SpringGuild
import uk.me.danielharman.kotlinspringbot.services.AttachmentService
import uk.me.danielharman.kotlinspringbot.services.GuildService

class SavePhraseCommand(private val guildService: GuildService, private val attachmentService: AttachmentService) : Command {
    override fun execute(event: GuildMessageReceivedEvent) {

        if (event.message.attachments.size > 0) {
            saveAttachment(event)
        } else {
            savePhrase(event)
        }


    }

    private fun savePhrase(event: GuildMessageReceivedEvent) {
        val content = event.message.contentRaw
        val split = content.split(" ")
        if (split.size < 3) {
            event.message.channel.sendMessage("Phrase missing").queue()
            return
        }

        if (split[1].contains(Regex("[_.!,?$\\\\-]"))) {
            event.message.channel.sendMessage("Cannot save with that phrase").queue()
            return
        }

        guildService.saveCommand(event.message.guild.id, split[1], split.subList(2, split.size).joinToString(" "), event.author.id)
        event.message.channel.sendMessage("Saved as ${split[1]}").queue()
    }

    private fun saveAttachment(event: GuildMessageReceivedEvent) {
        val content = event.message.contentRaw
        val split = content.split(" ")

        val attachment = event.message.attachments[0]

        guildService.saveCommand(event.message.guild.id, split[1], attachment.fileName, event.author.id, SpringGuild.CommandType.FILE)
        attachmentService.saveFile(attachment.retrieveInputStream().get(), event.message.guild.id, attachment.fileName)
        event.message.channel.sendMessage("Saved as ${split[1]}").queue()
    }

}