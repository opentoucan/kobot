package uk.me.danielharman.kotlinspringbot.command

import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.messages.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.models.DiscordCommand.CommandType.FILE
import uk.me.danielharman.kotlinspringbot.models.DiscordCommand.CommandType.STRING
import uk.me.danielharman.kotlinspringbot.services.AttachmentService
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

class SendCustomCommand(
    private val springGuildService: SpringGuildService,
    private val attachmentService: AttachmentService,
    private val commandService: DiscordCommandService,
    private val command: String
) : Command("", "") {

    override fun matchCommandString(str: String): Boolean = false

    override fun execute(event: DiscordMessageEvent) {

        when (val getGuild = springGuildService.getGuild(event.guild?.id ?: "")) {
            is Failure -> event.reply(Embeds.createErrorEmbed("Guild not found"))
            is Success -> {
                when (val customCommand = commandService.getCommand(getGuild.value.guildId, command)) {
                    is Failure -> event.reply(Embeds.createErrorEmbed("Command not found"))
                    is Success -> {
                        when (customCommand.value.type) {
                            STRING -> event.reply(customCommand.value.content ?: "")
                            FILE -> {
                                when (val file = attachmentService.getFile(
                                    event.guild?.id ?: "",
                                    customCommand.value.fileName ?: "",
                                    command
                                )) {
                                    is Failure -> event.reply(file.reason)
                                    is Success -> event.channel.sendFile(file.value, customCommand.value.fileName ?: "")
                                        .queue()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}