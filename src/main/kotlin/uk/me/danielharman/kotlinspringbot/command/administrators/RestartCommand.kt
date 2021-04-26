package uk.me.danielharman.kotlinspringbot.command.administrators

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand
import uk.me.danielharman.kotlinspringbot.services.admin.AdministratorService

@Component
class RestartCommand(private val administratorService: AdministratorService) : IAdminCommand {

    private val commandString = "restart"

    override fun execute(event: PrivateMessageReceivedEvent) {

        val getAdmin = administratorService.getBotAdministratorByDiscordId(event.author.id)

        if(getAdmin.failure){
            event.channel.sendMessage("You are not an admin").queue()
            return
        }

        event.channel.sendMessage("Restarting").complete()
        administratorService.restartDiscordConnection(getAdmin.value?.id ?: "")
    }
    override fun matchCommandString(str: String): Boolean = str == commandString
    override fun getCommandString(): String = commandString
}