package uk.me.danielharman.kotlinspringbot.command.administrators

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.admin.enums.Role
import uk.me.danielharman.kotlinspringbot.services.admin.AdministratorService

private const val MIN_REQUIRED_PARAMS = 3

@Component
class RemoveRoleCommand(
    private val administratorService: AdministratorService,
) : IAdminCommand {
    private val commandString = "removerole"
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun execute(event: MessageReceivedEvent) {
        when (administratorService.getBotAdministratorByDiscordId(event.author.id)) {
            is Failure ->
                event.channel
                    .sendMessageEmbeds(Embeds.createErrorEmbed("You are not an admin."))
                    .queue()
            is Success -> {
                val split = event.message.contentRaw.split(' ')

                if (split.size < MIN_REQUIRED_PARAMS) {
                    event.channel
                        .sendMessageEmbeds(Embeds.createErrorEmbed("Not enough parameters"))
                        .queue()
                    return
                }

                val user = event.jda.getUserByTag(split[1])

                if (user == null) {
                    event.channel
                        .sendMessageEmbeds(Embeds.createErrorEmbed("User not found"))
                        .queue()
                    return
                }

                try {
                    val role = Role.valueOf(split[2])

                    val addRole = administratorService.removeRole(event.author.id, user.id, role)

                    if (addRole is Failure) {
                        event.channel
                            .sendMessageEmbeds(Embeds.createErrorEmbed(addRole.reason))
                            .queue()
                        return
                    }
                } catch (e: IllegalArgumentException) {
                    logger.error(e.message)
                    event.channel
                        .sendMessageEmbeds(
                            Embeds.createErrorEmbed(
                                "No such role ${split[2]}. The current available roles are: ${
                                    Role.entries.fold("") { acc, r -> "$acc $r" }
                                }",
                            ),
                        ).queue()
                    return
                }

                event.channel
                    .sendMessageEmbeds(
                        Embeds
                            .infoEmbedBuilder()
                            .appendDescription("Removed ${split[2]} from ${user.asTag}")
                            .build(),
                    ).queue()
            }
        }
    }

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString
}
