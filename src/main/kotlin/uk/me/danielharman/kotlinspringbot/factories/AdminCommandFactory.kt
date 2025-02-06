package uk.me.danielharman.kotlinspringbot.factories

import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.command.administrators.DefaultAdminCommand
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand

@Service
class AdminCommandFactory(
    private val commands: List<IAdminCommand>,
) {
    fun getCommand(commandString: String): IAdminCommand {
        for (command in commands) {
            if (command.matchCommandString(commandString)) return command
        }
        return DefaultAdminCommand(commandString)
    }
}
