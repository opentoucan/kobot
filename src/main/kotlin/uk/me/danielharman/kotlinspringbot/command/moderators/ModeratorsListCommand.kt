package uk.me.danielharman.kotlinspringbot.command.moderators

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IModeratorCommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.PURPLE
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.properties.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

@Component
class ModeratorsListCommand(
    private val springGuildService: SpringGuildService,
    private val properties: KotlinBotProperties,
) : IModeratorCommand {
    private val commandString: String = "moderators"

    override fun matchCommandString(str: String): Boolean = commandString == str

    override fun getCommandString(): String = commandString

    override fun execute(event: MessageReceivedEvent) {
        event.channel.sendMessageEmbeds(createAdminUsersEmbed(event)).queue()
    }

    private fun createAdminUsersEmbed(message: MessageReceivedEvent): MessageEmbed = when (val guild = springGuildService.getGuild(message.guild.id)) {
        is Failure -> Embeds.createErrorEmbed("Could not find data for ${message.guild.name}")

        is Success -> {
            val primaryAdmin = message.guild.getMemberById(properties.primaryPrivilegedUserId)
                ?.effectiveName
                ?: properties.primaryPrivilegedUserId.ifBlank { "Unknown" }
            val moderators = guild.value.privilegedUsers.joinToString("\n") { userId ->
                message.guild.getMemberById(userId)?.effectiveName ?: userId
            }

            EmbedBuilder()
                .appendDescription("Bot controller: $primaryAdmin\n$moderators")
                .setColor(PURPLE)
                .setTitle("Moderators for ${message.guild.name}")
                .build()
        }
    }
}
