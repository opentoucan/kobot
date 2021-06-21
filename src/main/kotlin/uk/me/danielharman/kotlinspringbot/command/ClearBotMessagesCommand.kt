package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.messages.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.services.DiscordService

@Component
class ClearBotMessagesCommand(
    private val discordService: DiscordService,
    private val properties: KotlinBotProperties
) : Command("clear", "Clear command invocations and bot messages") {

    fun execute(event: GuildMessageReceivedEvent) {

    }

    override fun execute(event: DiscordMessageEvent) {

        val selfId = when(val selfUser = discordService.getSelfUser()){
            is Failure -> ""
            is Success -> selfUser.value.id
        }

        event.channel.history.retrievePast(50).complete().forEach { m ->
            if ((m.author.isBot)
                || m.author.id == selfId
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