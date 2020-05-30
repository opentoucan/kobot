package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class ShowAvatarCommand: Command {
    override fun execute(event: GuildMessageReceivedEvent) {
        val mentionedUsers = event.message.mentionedUsers

        if (mentionedUsers.size < 0) {
            event.channel.sendMessage("No users specified").queue()
        }
        mentionedUsers.forEach { u ->
            event.channel.sendMessage(EmbedBuilder()
                    .setTitle("Avatar")
                    .setAuthor(u.asTag)
                    .setImage("${u.avatarUrl}?size=512")
                    .build()
            ).queue()
        }
    }
}