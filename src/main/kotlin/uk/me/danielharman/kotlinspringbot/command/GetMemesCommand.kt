package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.helpers.JDAHelperFunctions.getChannelName
import uk.me.danielharman.kotlinspringbot.services.MemeService

class GetMemesCommand(private val memeService: MemeService) : Command {

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

            val description = EmbedBuilder().setImage(meme.url)
                    .setTitle("#$i")
                    .setAuthor(event.guild.getMemberById(meme.userId)?.nickname ?: meme.userId)
                    .setDescription("Channel: ${getChannelName(event.jda, meme.channelId)}\nUpvotes: ${meme.upvotes} Downvotes: ${meme.downvotes}")

            event.channel.sendMessage(description.build()).queue()
            i++
        }

    }

}