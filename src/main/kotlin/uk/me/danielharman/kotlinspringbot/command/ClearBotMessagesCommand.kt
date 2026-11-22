package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.helpers.getOrElse
import uk.me.danielharman.kotlinspringbot.helpers.map
import uk.me.danielharman.kotlinspringbot.properties.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.services.DiscordActionService

private const val HISTORY_LIMIT = 50

@Component
class ClearBotMessagesCommand(
    private val discordService: DiscordActionService,
    private val properties: KotlinBotProperties,
) : Command("clear", "Clear command invocations and bot messages"),
    ISlashCommand {
    override fun execute(event: DiscordMessageEvent) {
        val selfId: String? =
            discordService.getSelfUser().map { it.id }.getOrElse { reason ->
                logger.warn("Could not get the bot's own user, only clearing other bot and command messages: $reason")
                null
            }

        event.channel.history.retrievePast(HISTORY_LIMIT).complete().forEach { m ->
            if (
                (m.author.isBot) ||
                m.author.id == selfId ||
                m.contentStripped.startsWith(properties.commandPrefix) ||
                m.contentStripped.startsWith(properties.privilegedCommandPrefix)
            ) {
                try {
                    m.delete().queue()
                } catch (e: InsufficientPermissionException) {
                    logger.warn(
                        "Tried to delete message but had insufficient permissions. ${e.message}",
                    )
                }
            }
        }
        event.reply("Deleting content", true)
    }
}
