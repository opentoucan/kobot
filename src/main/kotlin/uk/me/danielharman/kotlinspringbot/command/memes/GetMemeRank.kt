package uk.me.danielharman.kotlinspringbot.command.memes

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.Param
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.messages.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.services.DiscordService
import uk.me.danielharman.kotlinspringbot.services.MemeService
import java.awt.Color

@Component
class GetMemeRank(private val memeService: MemeService, private val discordService: DiscordService) : Command(
    "memeranking",
    "List server members by their meme score",
    listOf(Param(0, "Sort", Param.ParamType.Text, "Sort direction"))
) {

    override fun execute(event: DiscordMessageEvent) {

        val split = event.content.split(' ')
        val asc = (split.size > 1 && split[1] == "asc")
        val memerIds = memeService.getMemerIds(event.guild?.id ?: "", asc)
        val des = StringBuilder()

        var counter = 1
        memerIds.forEach { pair ->
            val name = event.guild!!.retrieveMemberById(pair.first).complete()?.nickname
                ?: (discordService.getUserById(pair.first) as Success).value.asTag
            des.append("${if (!asc) "#" else ""}${if (!asc) counter++.toString() else ""} $name: S:${pair.second.score}   U:${pair.second.upvotes}   D:${pair.second.downvotes} of ${pair.second.count} posts\n")
        }
        event.reply(
            EmbedBuilder()
                .setTitle(if (asc) "Losers in ${event.guild?.name}" else "Meme ranking for ${event.guild?.name}")
                .setColor(Color.GRAY)
                .appendDescription(des.toString()).build()
        )
    }


}