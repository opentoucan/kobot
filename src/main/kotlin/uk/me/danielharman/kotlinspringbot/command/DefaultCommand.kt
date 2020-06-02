package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.models.SpringGuild.CommandType.FILE
import uk.me.danielharman.kotlinspringbot.models.SpringGuild.CommandType.STRING
import uk.me.danielharman.kotlinspringbot.services.AttachmentService
import uk.me.danielharman.kotlinspringbot.services.GuildService

class DefaultCommand(private val guildService: GuildService, private val attachmentService: AttachmentService, private val command: String) : Command {
    override fun execute(event: GuildMessageReceivedEvent) {

        val customCommand = guildService.getCommand(event.guild.id, command)

        if (customCommand != null) {

            when (customCommand.type) {
                STRING -> event.channel.sendMessage(customCommand.value).queue()
                FILE -> {
                    val file = attachmentService.getFile(event.guild.id, customCommand.value)
                    event.channel.sendFile(file, customCommand.value).queue()
                }
            }

        } else {
            event.channel.sendMessage("Command not found").queue()
        }

    }
}