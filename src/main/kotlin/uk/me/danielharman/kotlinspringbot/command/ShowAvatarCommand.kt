package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.ICommand

@Component
class ShowAvatarCommand: ICommand {

    private val commandString = "avatar"
    private val description = "Get a user's avatar"

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString

    override fun getCommandDescription(): String = description

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