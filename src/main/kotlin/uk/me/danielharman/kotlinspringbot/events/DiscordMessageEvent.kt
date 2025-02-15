package uk.me.danielharman.kotlinspringbot.events

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import java.io.InputStream

abstract class DiscordMessageEvent(
    content: String,
    val channel: MessageChannel,
    val author: User,
    val guild: Guild?,
    val attachments: List<Message.Attachment> = listOf(),
    val mentionedUsers: List<User> = listOf(),
) : MessageEvent(content, OriginService.Discord) {
    abstract fun reply(
        embed: MessageEmbed,
        invokerOnly: Boolean = false,
    )

    abstract fun reply(
        file: InputStream,
        filename: String,
    )
}
