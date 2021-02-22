package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.models.DiscordCommand.CommandType.FILE
import uk.me.danielharman.kotlinspringbot.models.DiscordCommand.CommandType.STRING
import uk.me.danielharman.kotlinspringbot.services.AttachmentService
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService
import uk.me.danielharman.kotlinspringbot.services.GuildService

class SendCustomCommand(private val guildService: GuildService, private val attachmentService: AttachmentService,
                        private val commandService: DiscordCommandService,
                        private val command: String) : Command {
    override fun execute(event: GuildMessageReceivedEvent) {

        val guild = guildService.getGuild(event.guild.id)

        if (guild == null)
        {
            event.channel.sendMessage(Embeds.createErrorEmbed("Guild not found")).queue()
            return
        }

        val customCommand = commandService.getCommand(guild.guildId, command)

        if (customCommand != null) {

            when (customCommand.type) {
                STRING -> event.channel.sendMessage(customCommand.content?: "").queue()
                FILE -> {
                    val file = attachmentService.getFile(event.guild.id, customCommand.fileName?: "", command)
                    event.channel.sendFile(file, customCommand.fileName?: "").queue()
                }
            }

        } else {
            event.channel.sendMessage(Embeds.createErrorEmbed("Command not found")).queue()
        }

    }
}