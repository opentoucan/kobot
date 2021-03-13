package uk.me.danielharman.kotlinspringbot.command.memes

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.ICommand
import uk.me.danielharman.kotlinspringbot.services.MemeService
import java.awt.Color

@Component
class GetMemeRank(private val memeService: MemeService) : ICommand {

    private val commandString = "memeranking"
    private val description = "List server members by their meme score"

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) {

        val split = event.message.contentStripped.split(' ')
        val asc = (split.size > 1 && split[1] == "asc" )
        val memerIds = memeService.getMemerIds(event.guild.id, asc)
        val des = StringBuilder()

        var counter = 1
        memerIds.forEach {
            pair ->
            val name = event.guild.retrieveMemberById(pair.first).complete()?.nickname
                    ?: event.jda.retrieveUserById(pair.first).complete()?.asTag
                    ?: pair.first
            des.append("${if(!asc) "#" else ""}${if (!asc) counter++.toString() else ""} $name: S:${pair.second.score}   U:${pair.second.upvotes}   D:${pair.second.downvotes} of ${pair.second.count} posts\n")
        }
        event.channel.sendMessage(EmbedBuilder()
                .setTitle(if (asc) "Losers in ${event.guild.name}" else "Meme ranking for ${event.guild.name}")
                .setColor(Color.GRAY)
                .appendDescription(des.toString()).build()).queue()
    }


}