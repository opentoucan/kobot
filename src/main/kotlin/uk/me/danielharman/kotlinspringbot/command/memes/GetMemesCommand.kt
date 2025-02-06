package uk.me.danielharman.kotlinspringbot.command.memes

import net.dv8tion.jda.api.EmbedBuilder
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import uk.me.danielharman.kotlinspringbot.models.Meme
import uk.me.danielharman.kotlinspringbot.services.DiscordActionService
import uk.me.danielharman.kotlinspringbot.services.MemeService
import java.util.Locale

@Component
class GetMemesCommand(
    private val memeService: MemeService,
    private val discordActionService: DiscordActionService,
) : Command(
    "memes",
    "List server memes by week or month",
    listOf(CommandParameter(0, "Interval", CommandParameter.ParamType.Word, "week or month")),
),
    ISlashCommand {
    override fun execute(event: DiscordMessageEvent) {
        val paramValue = event.getParamValue(commandParameters[0])
        val intervalString = paramValue.asString()

        var interval = MemeService.MemeInterval.WEEK

        if (!paramValue.error && intervalString != null) {
            interval =
                when (intervalString.lowercase(Locale.getDefault())) {
                    "month" -> MemeService.MemeInterval.MONTH
                    else -> MemeService.MemeInterval.WEEK
                }
        }
        val memes = memeService.getTop3ByInterval(event.guild?.id ?: "", interval)

        if (memes.isEmpty()) {
            event.reply("No memes found")
            return
        }

        var i = 1
        for (meme in memes) {
            val description =
                EmbedBuilder()
                    .setTitle("#$i")
                    .setAuthor(
                        event.guild!!
                            .retrieveMemberById(meme.userId)
                            .complete()
                            ?.nickname
                            ?: meme.userId,
                    )

            val channelName =
                when (val channel = discordActionService.getTextChannel(meme.channelId)) {
                    is Failure -> "Channel not found"
                    is Success -> channel.value.name
                }

            when (meme.urlType) {
                Meme.UrlType.Image -> {
                    description
                        .setImage(meme.url)
                        .setDescription(
                            "Channel: $channelName\nUpvotes: ${meme.upvotes} Downvotes: ${meme.downvotes}",
                        )
                }
                Meme.UrlType.Link -> {
                    description.setDescription(
                        "Channel: $channelName\nUpvotes: ${meme.upvotes} Downvotes: ${meme.downvotes} \n ${meme.url}",
                    )
                }
            }
            event.reply(description.build())
            i++
        }
    }
}
