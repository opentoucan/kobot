package uk.me.danielharman.kotlinspringbot.command.administrators

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.apache.commons.lang3.time.DurationFormatUtils
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.services.admin.AdministratorService
import java.time.Duration
import java.time.LocalDateTime

@Component
class BotInfoCommand(private val administratorService: AdministratorService) : IAdminCommand {

    private val commandString = "info"

    override fun execute(event: MessageReceivedEvent) {

        val botStartTime = when(val r = administratorService.getBotStartTime()){
            is Failure -> LocalDateTime.now()
            is Success -> r.value
        }
        val botVersion = when(val r = administratorService.getBotVersion()){
            is Failure -> r.reason
            is Success -> r.value
        }
        val duration = Duration.between(botStartTime, LocalDateTime.now())

        val build = Embeds.infoEmbedBuilder()
            .appendDescription("Bot version: ${botVersion}\n Bot uptime: ${DurationFormatUtils.formatDuration(duration.toMillis(), "[M 'months'] [d 'days'] [H 'hours'] [m 'minutes'] s 'seconds'")}")
            .build()
        event.channel.sendMessageEmbeds(build).queue()

    }
    override fun matchCommandString(str: String): Boolean = str == commandString
    override fun getCommandString(): String = commandString
}