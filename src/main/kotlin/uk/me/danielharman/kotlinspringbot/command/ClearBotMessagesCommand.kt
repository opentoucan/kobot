package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.command.interfaces.ICommand

@Component
class ClearBotMessagesCommand(private val properties: KotlinBotProperties) : ICommand {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val commandString = "clear"
    private val description = "Clear command invocations and bot messages"

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) {
        event.channel.history.retrievePast(50).complete().forEach { m ->
            if ((m.author.isBot)
                || m.author.id == event.jda.selfUser.id
                || m.contentStripped.startsWith(properties.commandPrefix)
                || m.contentStripped.startsWith(properties.privilegedCommandPrefix)
            ) {
                try {
                    m.delete().queue()
                } catch (e: InsufficientPermissionException) {
                    logger.warn("Tried to delete message but had insufficient permissions. ${e.message}")
                }
            }
        }
    }
}