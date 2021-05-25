package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.ICommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.helpers.toJavaZonedDateTime
import uk.me.danielharman.kotlinspringbot.models.DiscordCommand.CommandType.FILE
import uk.me.danielharman.kotlinspringbot.services.AttachmentService
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService
import java.awt.Color

@Component
class RandomDiscordCommand(
    private val springGuildService: SpringGuildService,
    private val attachmentService: AttachmentService,
    private val commandService: DiscordCommandService
) : ICommand {

    private val commandString = "random"
    private val description = "Send a random command"

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) {

        when (val getGuild = springGuildService.getGuild(event.guild.id)) {
            is Failure -> event.channel.sendMessage(Embeds.createErrorEmbed("Guild not found")).queue()
            is Success -> {
                val guild = getGuild.value
                when (val customCommand = commandService.getRandomCommand(guild.guildId)) {
                    is Failure -> event.channel.sendMessage(Embeds.createErrorEmbed("No commands found"))
                    is Success -> {

                        val complete = event.jda.retrieveUserById(customCommand.value.creatorId).complete()
                        val member = event.guild.getMember(complete)
                        val name = member?.nickname ?: complete.name

                        event.channel.sendMessage(
                            EmbedBuilder()
                                .setAuthor(name, complete.effectiveAvatarUrl, complete.effectiveAvatarUrl)
                                .appendDescription(customCommand.value.content ?: customCommand.value.fileName ?: "")
                                .setTitle(customCommand.value.key)
                                .setTimestamp(customCommand.value.created.toJavaZonedDateTime())
                                .setColor(Color.YELLOW)
                                .build()
                        ).queue()

                        if (customCommand.value.type === FILE) {
                            when (val file = attachmentService.getFile(
                                event.guild.id,
                                customCommand.value.fileName ?: "",
                                customCommand.value.key
                            )) {
                                is Failure -> event.channel.sendMessage(file.reason).queue()
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