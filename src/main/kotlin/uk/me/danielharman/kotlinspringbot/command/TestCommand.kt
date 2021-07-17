package uk.me.danielharman.kotlinspringbot.command

import org.joda.time.format.ISODateTimeFormat
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.objects.ApplicationInfo
import uk.me.danielharman.kotlinspringbot.services.DiscordActionService
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService
import uk.me.danielharman.kotlinspringbot.services.YoutubeService

@Component
class TestCommand(private val youtubeService: YoutubeService) :
    Command(
        "test",
        "Bot information",
        listOf(CommandParameter(0, "Command", CommandParameter.ParamType.Word, "Name of command to inspect", false))
    ), ISlashCommand {

    override fun execute(event: DiscordMessageEvent) {

       youtubeService.getAudio("https://www.youtube.com/watch?v=cV9BtuPpW9w")
    }
}