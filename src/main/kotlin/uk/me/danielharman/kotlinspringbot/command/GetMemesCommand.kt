package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.services.MemeService

class GetMemesCommand(private val memeService: MemeService) : Command {

    override fun execute(event: GuildMessageReceivedEvent) {

        val split = event.message.contentStripped.split(" ")

        val interval: MemeService.MemeInterval
        if (split.size >= 2){
            interval = when (split[1].toLowerCase()){
                "week" -> MemeService.MemeInterval.WEEK
                "month" -> MemeService.MemeInterval.MONTH
                else -> {
                    event.channel.sendMessage("Please give an interval (month/week)").queue()
                    return
                }
            }
        }
        else{
            event.channel.sendMessage("Please give an interval (month/week)").queue()
            return
        }
        val memes = memeService.getTop3ByInterval(event.guild.id, interval)

        if (memes.isEmpty()){
            event.channel.sendMessage("No memes found").queue()
            return
        }

        var i = 1
        for (meme in memes){

            val description = EmbedBuilder().setImage(meme.url)
                    .setTitle("#$i")
                    .setAuthor(event.guild.getMemberById(meme.userId)?.nickname ?: meme.userId)
                    .setDescription("Upvotes: ${meme.upvotes} Downvotes: ${meme.downvotes}")

            event.channel.sendMessage(description.build()).queue()
            i++
        }

   }

}