package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import uk.me.danielharman.kotlinspringbot.ApplicationLogger

class ClearBotMessagesCommand(private val commandPrefix: String, private val privilegedCommandPrefix: String): Command {
    override fun execute(event: GuildMessageReceivedEvent) {
        event.channel.history.retrievePast(50).complete().forEach { m ->
            if ((m.author.isBot)
                    || m.author.id == event.jda.selfUser.id
                    || m.contentStripped.startsWith(commandPrefix)
                    || m.contentStripped.startsWith(privilegedCommandPrefix)) {
                try {
                    m.delete().queue()
                } catch (e: InsufficientPermissionException) {
                    ApplicationLogger.logger.warn("Tried to delete message but had insufficient permissions. ${e.message}")
                }
            }
        }
    }
}