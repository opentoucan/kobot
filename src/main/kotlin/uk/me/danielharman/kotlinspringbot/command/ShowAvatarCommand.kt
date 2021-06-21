package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.Param
import uk.me.danielharman.kotlinspringbot.messages.DiscordMessageEvent

@Component
class ShowAvatarCommand :
    Command(
        "avatar",
        "Get a user's avatar",
        listOf(Param(0, "Usertag", Param.ParamType.Mentionable, "User tag", true))
    ) {

    override fun execute(event: DiscordMessageEvent) {
        val mentionedUsers = event.mentionedUsers

        if (mentionedUsers.size < 0) {
            event.reply("No users specified")
        }
        mentionedUsers.forEach { u ->
            event.reply(
                EmbedBuilder()
                    .setTitle("Avatar")
                    .setAuthor(u.asTag)
                    .setImage("${u.avatarUrl}?size=512")
                    .build()
            )
        }
    }

}