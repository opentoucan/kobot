package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.joda.time.format.ISODateTimeFormat
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.Param
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.messages.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.objects.ApplicationInfo
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService
import uk.me.danielharman.kotlinspringbot.services.DiscordService

@Component
class InfoCommand(private val commandService: DiscordCommandService, private val discordService: DiscordService) :
    Command(
        "info",
        "Bot information",
        listOf(Param(0, "Command", Param.ParamType.Text, "Name of command to inspect", false))
    ) {

    override fun execute(event: DiscordMessageEvent) {
        val split = event.content.split(" ")

        if (split.size > 1) {

            when (val command = commandService.getCommand(event.guild?.id ?: "", split[1])) {
                is Failure -> event.channel.sendMessage(Embeds.createErrorEmbed("Command not found")).queue()
                is Success -> {
                    val creatorName = if (command.value.creatorId.isEmpty())
                        "Unknown"
                    else {
                        when (val user = discordService.getUserById(command.value.creatorId)) {
                            is Failure -> "Unknown"
                            is Success -> user.value.asTag
                        }
                    }
                    event.channel.sendMessage(
                        Embeds.infoEmbedBuilder(title = "Command: ${split[1]}")
                            .appendDescription(command.value.content ?: command.value.fileName ?: "No Content")
                            .addField("Creator", creatorName, false)
                            .addField(
                                "Created",
                                command.value.created.toString(ISODateTimeFormat.dateTimeNoMillis()),
                                false
                            )
                            .build()
                    ).queue()
                }
            }
        } else {

            event.channel.sendMessage(
                Embeds.infoEmbedBuilder(title = "KotBot")
                    .appendDescription("This is a Discord bot written in Kotlin using Spring")
                    .addField("Version", ApplicationInfo.version, false)
                    .addField("Developers", "Daniel Harman\nKieran Dennis\nJared Prest", false)
                    .addField("Libraries", "https://spring.io, https://kotlinlang.org", false)
                    .addField("Source", "https://gitlab.com/update-gitlab.yml/kotlinspringbot", false)
                    .addField("Licence", "https://www.apache.org/licenses/LICENSE-2.0", false)
                    .build()
            ).queue()
        }
    }
}