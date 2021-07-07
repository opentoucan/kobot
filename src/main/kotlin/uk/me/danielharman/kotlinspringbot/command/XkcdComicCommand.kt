package uk.me.danielharman.kotlinspringbot.command

import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Embeds.createXkcdComicEmbed
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.services.XkcdService

@Component
class XkcdComicCommand(private val xkcdService: XkcdService) : Command(
    "xkcd",
    "Get the latest XKCD comic or a particular comic by number",
    listOf(CommandParameter(0, "ComicNumber", CommandParameter.ParamType.Long, "Comic number", false))
), ISlashCommand {

    override fun execute(event: DiscordMessageEvent) {

        val paramValue = event.getParamValue(commandParameters[0])

        val comicNumber = paramValue.asLong()

        if (comicNumber == null && !paramValue.error) {
            val message = when (val xkcd = xkcdService.getLatestComic()) {
                is Failure -> Embeds.createErrorEmbed(xkcd.reason)
                is Success -> createXkcdComicEmbed(xkcd.value, "Latest Comic")
            }
            event.reply(message)
            return
        }

        if (paramValue.error || comicNumber ?: -1 <= 0) {
            event.reply("Not a valid comic number.")
            return
        }

        when (val xkcd = xkcdService.getComic(comicNumber)) {
            is Failure -> event.reply("Comic not found.")
            is Success -> event.reply(createXkcdComicEmbed(xkcd.value))
        }
    }

}