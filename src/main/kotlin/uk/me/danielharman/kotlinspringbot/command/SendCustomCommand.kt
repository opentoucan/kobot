package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.command.interfaces.ICommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.DiscordCommand.CommandType.FILE
import uk.me.danielharman.kotlinspringbot.models.DiscordCommand.CommandType.STRING
import uk.me.danielharman.kotlinspringbot.services.AttachmentService
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

class SendCustomCommand(private val springGuildService: SpringGuildService,
                        private val attachmentService: AttachmentService,
                        private val commandService: DiscordCommandService,
                        private val command: String) : ICommand {

    override fun matchCommandString(str: String): Boolean = false

    override fun getCommandString(): String = ""

    override fun getCommandDescription(): String = ""

    override fun execute(event: GuildMessageReceivedEvent) {

        when(val getGuild = springGuildService.getGuild(event.guild.id)){
            is Failure -> event.channel.sendMessage(Embeds.createErrorEmbed("Guild not found")).queue()
            is Success -> {
                val customCommand = commandService.getCommand(getGuild.value.guildId, command)

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
    }
}