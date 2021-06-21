package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.helpers.toJavaZonedDateTime
import uk.me.danielharman.kotlinspringbot.messages.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.models.DiscordCommand.CommandType.FILE
import uk.me.danielharman.kotlinspringbot.services.AttachmentService
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService
import uk.me.danielharman.kotlinspringbot.services.DiscordService
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService
import java.awt.Color

@Component
class RandomDiscordCommand(
    private val springGuildService: SpringGuildService,
    private val attachmentService: AttachmentService,
    private val commandService: DiscordCommandService,
    private val discordService: DiscordService
) : Command("random", "Send a random command") {

    override fun execute(event: DiscordMessageEvent) {

        when (val getGuild = springGuildService.getGuild(event.guild?.id ?: "")) {
            is Failure -> event.channel.sendMessage(Embeds.createErrorEmbed("Guild not found")).queue()
            is Success -> {
                val guild = getGuild.value
                when (val customCommand = commandService.getRandomCommand(guild.guildId)) {
                    is Failure -> event.channel.sendMessage(Embeds.createErrorEmbed("No commands found"))
                    is Success -> {

                        val user = when (val user = discordService.getUserById(customCommand.value.creatorId)) {
                            is Failure -> null
                            is Success -> user.value
                        }
                        val member: Member?
                        var name = ""

                        if (user != null) {

                            member = event.guild?.getMember(user)
                            name = member?.nickname ?: user.name
                        }
                        event.channel.sendMessage(
                            EmbedBuilder()
                                .setAuthor(name, user?.effectiveAvatarUrl ?: "", user?.effectiveAvatarUrl ?: "")
                                .appendDescription(customCommand.value.content ?: customCommand.value.fileName ?: "")
                                .setTitle(customCommand.value.key)
                                .setTimestamp(customCommand.value.created.toJavaZonedDateTime())
                                .setColor(Color.YELLOW)
                                .build()
                        ).queue()

                        if (customCommand.value.type === FILE) {
                            when (val file = attachmentService.getFile(
                                event.guild?.id ?: "",
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