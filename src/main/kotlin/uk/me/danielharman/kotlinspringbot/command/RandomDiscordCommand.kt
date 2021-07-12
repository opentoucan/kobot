package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.helpers.toJavaZonedDateTime
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.models.DiscordCommand.CommandType.FILE
import uk.me.danielharman.kotlinspringbot.services.*
import java.awt.Color

@Component
class RandomDiscordCommand(
    private val springGuildService: SpringGuildService,
    private val attachmentService: AttachmentService,
    private val commandService: DiscordCommandService,
    private val discordService: DiscordActionService
) : Command("random", "Send a random command"), ISlashCommand {

    override fun execute(event: DiscordMessageEvent) {
        if (event.guild == null) {
            event.reply(Embeds.createErrorEmbed("This command can only be used in Servers"))
            return
        }

        when (val getGuild = springGuildService.getGuild(event.guild.id)) {
            is Failure -> event.reply(Embeds.createErrorEmbed("Guild not found"))
            is Success -> {
                val guild = getGuild.value
                when (val customCommand = commandService.getRandomCommand(guild.guildId)) {
                    is Failure -> event.reply(Embeds.createErrorEmbed("No commands found"))
                    is Success -> {

                        val user = when (val user = discordService.getUserById(customCommand.value.creatorId)) {
                            is Failure -> null
                            is Success -> user.value
                        }
                        val member: Member?
                        var name = ""

                        if (user != null) {
                            member = event.guild.getMember(user)
                            name = member?.nickname ?: user.name
                        }
                        event.reply(
                            EmbedBuilder()
                                .setAuthor(name, user?.effectiveAvatarUrl ?: "", user?.effectiveAvatarUrl ?: "")
                                .appendDescription(customCommand.value.content ?: customCommand.value.fileName ?: "")
                                .setTitle(customCommand.value.key)
                                .setTimestamp(customCommand.value.created.toJavaZonedDateTime())
                                .setColor(Color.YELLOW)
                                .build()
                        )

                        if (customCommand.value.type === FILE) {
                            when (val file = attachmentService.getFile(
                                event.guild.id,
                                customCommand.value.fileName ?: "",
                                customCommand.value.key
                            )) {
                                is Failure -> event.reply(file.reason)
                                is Success -> event.reply(file.value, customCommand.value.fileName ?: "")
                            }
                        }
                    }
                }
            }
        }
    }
}