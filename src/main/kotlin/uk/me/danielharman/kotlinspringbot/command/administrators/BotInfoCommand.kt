package uk.me.danielharman.kotlinspringbot.command.administrators

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.apache.commons.lang3.time.DurationFormatUtils
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.getOrElse
import uk.me.danielharman.kotlinspringbot.helpers.map
import uk.me.danielharman.kotlinspringbot.services.admin.AdministratorService
import java.time.Duration
import java.time.LocalDateTime

@Component
class BotInfoCommand(
    private val administratorService: AdministratorService,
) : IAdminCommand {
    private val commandString = "info"

    override fun execute(event: MessageReceivedEvent) {
        val botUptime =
            administratorService
                .getBotStartTime()
                .map { startTime ->
                    DurationFormatUtils.formatDuration(
                        Duration.between(startTime, LocalDateTime.now()).toMillis(),
                        "[M 'months'] [d 'days'] [H 'hours'] [m 'minutes'] s 'seconds'",
                    )
                }.getOrElse { reason -> "unavailable ($reason)" }
        val botVersion = administratorService.getBotVersion().getOrElse { reason -> "unavailable ($reason)" }

        val build =
            Embeds
                .infoEmbedBuilder()
                .appendDescription("Bot version: ${botVersion}\n Bot uptime: $botUptime")
                .build()
        event.channel.sendMessageEmbeds(build).queue()
    }

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString
}
