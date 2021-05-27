package uk.me.danielharman.kotlinspringbot.command.memes

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.ICommand
import uk.me.danielharman.kotlinspringbot.helpers.JDAHelperFunctions.getChannelName
import uk.me.danielharman.kotlinspringbot.models.Meme
import uk.me.danielharman.kotlinspringbot.services.MemeService

@Component
class GetMemesCommand(private val memeService: MemeService) : ICommand {

    private val commandString = "memes"
    private val description = "List server memes by week or month"

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) {

        val split = event.message.contentStripped.split(" ")

        var interval = MemeService.MemeInterval.WEEK

        if (split.size > 1) {
            interval = when (split[1].toLowerCase()) {
                "month" -> MemeService.MemeInterval.MONTH
                else -> MemeService.MemeInterval.WEEK
            }
        }
        val memes = memeService.getTop3ByInterval(event.guild.id, interval)

        if (memes.isEmpty()) {
            event.channel.sendMessage("No memes found").queue()
            return
        }

        var i = 1
        for (meme in memes) {

            val description = EmbedBuilder().setTitle("#$i").setAuthor(event.guild.retrieveMemberById(meme.userId).complete()?.nickname
                    ?: meme.userId)
            when (meme.urlType) {
                Meme.UrlType.Image -> {
                    description.setImage(meme.url)
                            .setDescription("Channel: ${getChannelName(event.jda, meme.channelId)}\nUpvotes: ${meme.upvotes} Downvotes: ${meme.downvotes}")
                }
                Meme.UrlType.Link -> {
                    description.setDescription("Channel: ${getChannelName(event.jda, meme.channelId)}\nUpvotes: ${meme.upvotes} Downvotes: ${meme.downvotes} \n ${meme.url}")
                }
            }
            event.channel.sendMessage(description.build()).queue()
            i++
        }

    }

}