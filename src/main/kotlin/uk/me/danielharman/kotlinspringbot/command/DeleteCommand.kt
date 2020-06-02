package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.models.SpringGuild.CommandType.FILE
import uk.me.danielharman.kotlinspringbot.services.AttachmentService
import uk.me.danielharman.kotlinspringbot.services.GuildService

class DeleteCommand(private val guildService: GuildService, private val attachmentService: AttachmentService) : Command {
    override fun execute(event: GuildMessageReceivedEvent) {

        val content = event.message.contentRaw
        val split = content.split(" ")

        if(split.size < 2)
        {
            event.channel.sendMessage("Command not found").queue()
            return
        }

        val command = split[1]

        val customCommand = guildService.getCommand(event.guild.id, command)

        if (customCommand != null) {

            guildService.deleteCommand(event.guild.id, command)

            if (customCommand.type == FILE)
                attachmentService.deleteAttachment(event.guild.id, customCommand.value, split[1])

            event.channel.sendMessage("Command deleted").queue()
        } else {
            event.channel.sendMessage("Command not found").queue()
        }


    }
}