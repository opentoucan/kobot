package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.Param
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Embeds.createXkcdComicEmbed
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.messages.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.services.XkcdService
import java.lang.NumberFormatException

@Component
class XkcdComicCommand(private val xkcdService: XkcdService) : Command(
    "xkcd",
    "Get the latest XKCD comic or a particular comic by number",
    listOf(Param(0, "ComicNumber", Param.ParamType.Int, "Comic number", false))
) {

    override fun execute(event: DiscordMessageEvent){

        val split = event.content.split(" ")

        if (split.size < 2) {
            val message = when (val xkcd = xkcdService.getLatestComic()) {
                is Failure -> Embeds.createErrorEmbed(xkcd.reason)
                is Success -> createXkcdComicEmbed(xkcd.value, "Latest Comic")
            }
            event.channel.sendMessage(message).queue()
            return
        }

        val comicNumber: Int
        try {
            comicNumber = Integer.parseInt(split[1])
        } catch (e: NumberFormatException) {
            event.reply("No number was given.")
            return
        }

        if (comicNumber <= 0) {
            event.reply("Not a valid comic number.")
            return
        }

        when (val xkcd = xkcdService.getComic(comicNumber)) {
            is Failure -> event.reply("Comic not found.")
            is Success -> event.channel.sendMessage(createXkcdComicEmbed(xkcd.value)).queue()
        }
    }

}