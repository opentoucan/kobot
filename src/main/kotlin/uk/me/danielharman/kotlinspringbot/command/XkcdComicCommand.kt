package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.ICommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Embeds.createXkcdComicEmbed
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.services.XkcdService
import java.lang.NumberFormatException

@Component
class XkcdComicCommand(private val xkcdService: XkcdService) : ICommand {

    private val commandString = "xkcd"
    private val description = "Get the latest XKCD comic or a particular comic by number"

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) {

        val split = event.message.contentStripped.split(" ")

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
            event.message.channel.sendMessage("No number was given.").queue()
            return
        }

        if (comicNumber <= 0) {
            event.message.channel.sendMessage("Not a valid comic number.").queue()
            return
        }

        when (val xkcd = xkcdService.getComic(comicNumber)) {
            is Failure -> event.message.channel.sendMessage("Comic not found.").queue()
            is Success -> event.channel.sendMessage(createXkcdComicEmbed(xkcd.value)).queue()
        }
    }
}