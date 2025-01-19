package uk.me.danielharman.kotlinspringbot.command

import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.objects.ApplicationInfo

@Component
class InfoCommand :
    Command(
        "info",
        "Bot information",
        listOf(CommandParameter(0, "Command", CommandParameter.ParamType.Word, "Name of command to inspect", false))
    ), ISlashCommand {

    override fun execute(event: DiscordMessageEvent) {
        event.reply(
            Embeds.infoEmbedBuilder(title = "KotBot")
                .appendDescription("This is a Discord bot written in Kotlin using Spring")
                .addField("Version", ApplicationInfo.version, false)
                .addField("Developers", "Daniel Harman\nKieran Dennis\nJared Prest", false)
                .addField("Libraries", "https://spring.io, https://kotlinlang.org", false)
                .addField("Source", "https://github.com/opentoucan/kobot", false)
                .addField("Licence", "https://www.apache.org/licenses/LICENSE-2.0", false)
                .build()
        )
    }
}