package uk.me.danielharman.kotlinspringbot.command.memes

import net.dv8tion.jda.api.EmbedBuilder
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import uk.me.danielharman.kotlinspringbot.services.MemeService
import java.awt.Color

@Component
class GetMemeRank(
    private val memeService: MemeService,
) : Command(
    "memeranking",
    "List server members by their meme score",
    listOf(CommandParameter(0, "Sort", CommandParameter.ParamType.Word, "Sort direction")),
),
    ISlashCommand {
    override fun execute(event: DiscordMessageEvent) {
        val guild = event.guild
        if (guild == null) {
            event.reply(Embeds.createErrorEmbed("This command can only be used in Servers"))
            return
        }

        val paramValue = event.getParamValue(commandParameters[0])
        val sort = paramValue.asString()

        val asc = (!paramValue.error && sort != null && sort.lowercase() == "asc")

        val rankings =
            when (val result = memeService.getMemerIds(guild.id, asc)) {
                is Failure -> {
                    event.reply(Embeds.createErrorEmbed("Failed to get meme ranking: ${result.reason}"))
                    return
                }

                is Success -> result.value
            }

        val ranking = rankings
            .mapIndexed { index, (userId, score) ->
                val name = guild.getMemberById(userId)?.effectiveName ?: userId
                val rank = if (asc) "" else "#${index + 1} "
                "$rank$name: S:${score.score}   U:${score.upvotes}   D:${score.downvotes} of ${score.count} posts"
            }.joinToString("\n")

        event.reply(
            EmbedBuilder()
                .setTitle(
                    if (asc) {
                        "Losers in ${guild.name}"
                    } else {
                        "Meme ranking for ${guild.name}"
                    },
                ).setColor(Color.GRAY)
                .appendDescription(ranking)
                .build(),
        )
    }
}
