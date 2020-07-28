package uk.me.danielharman.kotlinspringbot.command.xkcd

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.joda.time.DateTime
import uk.me.danielharman.kotlinspringbot.command.Command
import uk.me.danielharman.kotlinspringbot.services.XkcdService
import java.util.*

@Serializable
data class XkcdModel(val month: Int,
                     val num: Int,
                     val link: String,
                     val year: String,
                     val news: String,
                     val safe_title: String,
                     val transcript: String,
                     val alt: String,
                     val img: String,
                     val title: String,
                     val day: Int)

class XkcdLatestCommand(private val xkcdService: XkcdService, private val xkcdLatestUrl: String): Command {
    override fun execute(event: GuildMessageReceivedEvent) {
        val xkcd = xkcdService.getComic(xkcdLatestUrl)
         val builder = EmbedBuilder()
                 .setImage(xkcd.img)
                 .addField("Comic Title", xkcd.title, true)
                 .addField("Publish Date", "${xkcd.day}/${xkcd.month}/${xkcd.year}", true)
                 .addField("Comic Alt Text", xkcd.alt, true)
                 .setTitle("Latest xkcd Comic: #${xkcd.num}")
                 .setColor(0x9d03fc)
        event.channel.sendMessage(builder.build()).queue()
    }
}