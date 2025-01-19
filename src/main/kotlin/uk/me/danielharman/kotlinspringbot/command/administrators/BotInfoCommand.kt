package uk.me.danielharman.kotlinspringbot.command.administrators

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.PeriodFormat
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.services.admin.AdministratorService

@Component
class BotInfoCommand(private val administratorService: AdministratorService) : IAdminCommand {

    private val commandString = "info"

    override fun execute(event: MessageReceivedEvent) {

        val botStartTime = when(val r = administratorService.getBotStartTime()){
            is Failure -> DateTime.now()
            is Success -> r.value
        }
        val botVersion = when(val r = administratorService.getBotVersion()){
            is Failure -> r.reason
            is Success -> r.value
        }
        val duration = Interval(botStartTime, DateTime.now()).toPeriod()

        val build = Embeds.infoEmbedBuilder()
            .appendDescription("Bot version: ${botVersion}\n Bot uptime: ${PeriodFormat.getDefault().print(duration)}")
            .build()
        event.channel.sendMessageEmbeds(build).queue()

    }
    override fun matchCommandString(str: String): Boolean = str == commandString
    override fun getCommandString(): String = commandString
}