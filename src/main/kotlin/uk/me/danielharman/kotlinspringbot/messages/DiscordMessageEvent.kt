package uk.me.danielharman.kotlinspringbot.messages

import net.dv8tion.jda.api.entities.*
import uk.me.danielharman.kotlinspringbot.command.interfaces.Param

class DiscordMessageEvent(
    content: String,
    val channel: MessageChannel,
    val type: Type,
    val author: User,
    val guild: Guild?,
    val attachments: List<Message.Attachment>,
    val mentionedUsers: List<User>
) : MessageEvent(content, OriginService.Discord) {

    enum class Type {
        ChannelMessage,
        ChannelSlashCommand,
        DirectMessage,
        DirectSlashCommand
    }

    override fun reply(msg: String) {
        channel.sendMessage(msg).queue()
    }

    fun reply(embed: MessageEmbed) {
        channel.sendMessage(embed).queue()
    }

    override fun parseParam(param: Param): String {
        TODO("Not yet implemented")
    }

}