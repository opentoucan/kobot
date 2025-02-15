package uk.me.danielharman.kotlinspringbot.factories

import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.command.interfaces.IModeratorCommand
import uk.me.danielharman.kotlinspringbot.command.moderators.DefaultModeratorCommand

@Service
class ModeratorCommandFactory(
    private val commands: List<IModeratorCommand>,
) {
    fun getCommand(commandString: String): IModeratorCommand {
        for (command in commands) {
            if (command.matchCommandString(commandString)) return command
        }
        return DefaultModeratorCommand(commandString)
    }
}
