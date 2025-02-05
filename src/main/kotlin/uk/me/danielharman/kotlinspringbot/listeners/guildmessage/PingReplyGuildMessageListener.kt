package uk.me.danielharman.kotlinspringbot.listeners.guildmessage

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

@Component
class PingReplyGuildMessageListener(
    private val springGuildService: SpringGuildService,
    ) : ListenerAdapter() {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!event.isFromGuild || event.author.isBot) return;

        logger.debug("[${event.guild.name}] #${event.channel.name} <${event.member?.nickname ?: event.author.asTag}>: ${event.message.contentDisplay}")

        val isDeafened = springGuildService.isChannelDeafened(event.guild.id, event.channel.id)

        if (!isDeafened && event.message.mentions.isMentioned(event.jda.selfUser, Message.MentionType.USER)) {
            val emotesByName = event.guild.getEmojisByName("piing", true)
            if (emotesByName.size >= 1)
                event.message.addReaction(emotesByName[0]).queue()
            else
                event.message.addReaction(Emoji.fromUnicode("U+1F621")).queue()
        }
    }
}
