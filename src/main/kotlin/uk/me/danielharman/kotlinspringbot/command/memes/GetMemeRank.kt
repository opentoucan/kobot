package uk.me.danielharman.kotlinspringbot.command.memes

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.services.DiscordActionService
import uk.me.danielharman.kotlinspringbot.services.MemeService
import java.awt.Color

@Component
class GetMemeRank(private val memeService: MemeService, private val discordService: DiscordActionService) : Command(
    "memeranking",
    "List server members by their meme score",
    listOf(CommandParameter(0, "Sort", CommandParameter.ParamType.Word, "Sort direction"))
), ISlashCommand {

    override fun execute(event: DiscordMessageEvent) {

        if (event.guild == null) {
            event.reply(Embeds.createErrorEmbed("This command can only be used in Servers"))
            return
        }

        val paramValue = event.getParamValue(commandParameters[0])
        val sort = paramValue.asString()

        val asc = (!paramValue.error && sort != null && sort.lowercase() == "asc")

        val memerIds = memeService.getMemerIds(event.guild.id, asc)
        val des = StringBuilder()

        var counter = 1
        memerIds.forEach { pair ->

            try {
                val member = event.guild.retrieveMemberById(pair.first).complete()
                val name = member?.nickname ?: when (val user = discordService.getUserById(pair.first)) {
                    is Failure -> "Unknown"
                    is Success -> user.value.asTag
                }
                des.append("${if (!asc) "#" else ""}${if (!asc) counter++.toString() else ""} $name: S:${pair.second.score}   U:${pair.second.upvotes}   D:${pair.second.downvotes} of ${pair.second.count} posts\n")
            } catch (e: ErrorResponseException) {
                logger.error(e.message)
            }
        }
        event.reply(
            EmbedBuilder()
                .setTitle(if (asc) "Losers in ${event.guild.name}" else "Meme ranking for ${event.guild.name}")
                .setColor(Color.GRAY)
                .appendDescription(des.toString()).build()
        )
    }


}