package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.ICommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.toJavaZonedDateTime
import uk.me.danielharman.kotlinspringbot.models.DiscordCommand.CommandType.FILE
import uk.me.danielharman.kotlinspringbot.services.AttachmentService
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService
import uk.me.danielharman.kotlinspringbot.services.GuildService
import java.awt.Color

@Component
class RandomDiscordCommand(
    private val guildService: GuildService,
    private val attachmentService: AttachmentService,
    private val commandService: DiscordCommandService
) : ICommand {

    private val commandString = "random"
    private val description = "Send a random command"

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) {

        val guild = guildService.getGuild(event.guild.id)

        if (guild == null) {
            event.channel.sendMessage(Embeds.createErrorEmbed("Guild not found")).queue()
            return
        }

        val customCommand = commandService.getRandomCommand(guild.guildId)

        if (customCommand != null) {

            val complete = event.jda.retrieveUserById(customCommand.creatorId).complete()
            val member = event.guild.getMember(complete)
            val name = member?.nickname ?: complete.name

            event.channel.sendMessage(
                EmbedBuilder()
                    .setAuthor(name, complete.effectiveAvatarUrl, complete.effectiveAvatarUrl)
                    .appendDescription(customCommand.content ?: customCommand.fileName ?: "")
                    .setTitle(customCommand.key)
                    .setTimestamp(customCommand.created.toJavaZonedDateTime())
                    .setColor(Color.YELLOW)
                    .build()
            ).queue()

            if (customCommand.type === FILE) {
                val file = attachmentService.getFile(event.guild.id, customCommand.fileName ?: "", customCommand.key)
                event.channel.sendFile(file, customCommand.fileName ?: "").queue()
            }

        } else {
            event.channel.sendMessage(Embeds.createErrorEmbed("No commands found")).queue()
        }

    }
}