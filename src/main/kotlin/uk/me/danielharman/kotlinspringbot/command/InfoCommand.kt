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

@Component
class InfoCommand(private val commandService: DiscordCommandService, private val discordService: DiscordActionService) :
    Command(
        "info",
        "Bot information",
        listOf(CommandParameter(0, "Command", CommandParameter.ParamType.Word, "Name of command to inspect", false))
    ), ISlashCommand {

    override fun execute(event: DiscordMessageEvent) {

        val paramValue = event.getParamValue(commandParameters[0])
        val commandName = paramValue.asString()

        if (!paramValue.error && commandName != null) {

            when (val command = commandService.getCommand(event.guild?.id ?: "", commandName)) {
                is Failure -> event.reply(Embeds.createErrorEmbed("Command not found"))
                is Success -> {
                    val creatorName = if (command.value.creatorId.isEmpty())
                        "Unknown"
                    else {
                        when (val user = discordService.getUserById(command.value.creatorId)) {
                            is Failure -> "Unknown"
                            is Success -> user.value.asTag
                        }
                    }
                    event.reply(
                        Embeds.infoEmbedBuilder(title = "Command: $commandName")
                            .appendDescription(command.value.content ?: command.value.fileName ?: "No Content")
                            .addField("Creator", creatorName, false)
                            .addField(
                                "Created",
                                command.value.created.toString(ISODateTimeFormat.dateTimeNoMillis()),
                                false
                            )
                            .build()
                    )
                }
            }
        } else {

            event.reply(
                Embeds.infoEmbedBuilder(title = "KotBot")
                    .appendDescription("This is a Discord bot written in Kotlin using Spring")
                    .addField("Version", ApplicationInfo.version, false)
                    .addField("Developers", "Daniel Harman\nKieran Dennis\nJared Prest", false)
                    .addField("Libraries", "https://spring.io, https://kotlinlang.org", false)
                    .addField("Source", "https://gitlab.com/update-gitlab.yml/kotlinspringbot", false)
                    .addField("Licence", "https://www.apache.org/licenses/LICENSE-2.0", false)
                    .build()
            )
        }
    }
}