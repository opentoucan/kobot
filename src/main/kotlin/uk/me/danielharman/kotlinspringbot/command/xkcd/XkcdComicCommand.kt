package uk.me.danielharman.kotlinspringbot.command.xkcd

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.command.Command
import uk.me.danielharman.kotlinspringbot.helpers.Embeds.createXkcdComicEmbed
import uk.me.danielharman.kotlinspringbot.services.XkcdService
import java.lang.NumberFormatException


class XkcdComicCommand(private val xkcdService: XkcdService) : Command {
    override fun execute(event: GuildMessageReceivedEvent) {

        val split = event.message.contentStripped.split(" ")

        if (split.size < 2) {
            val xkcd = xkcdService.getLatestComic()
            event.channel.sendMessage(createXkcdComicEmbed(xkcd, "Latest Comic")).queue()
            return
        }

        val comicNumber: Int
        try {
            comicNumber = Integer.parseInt(split[1])
        } catch (e: NumberFormatException) {
            event.message.channel.sendMessage("No number was given.").queue()
            return
        }

        if (comicNumber <= 0) {
            event.message.channel.sendMessage("Not a valid comic number.").queue()
            return
        }

        val xkcd = xkcdService.getComic(comicNumber)

        if (xkcd == null) {
            event.message.channel.sendMessage("Comic not found.").queue()
            return
        }

        event.channel.sendMessage(createXkcdComicEmbed(xkcd)).queue()
    }
}