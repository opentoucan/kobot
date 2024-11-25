package uk.me.danielharman.kotlinspringbot.factories

import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.services.AttachmentService
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

@Service
class CommandFactory(
    private val commands: List<Command>
) {

    fun getCommand(commandString: String): Command? {
        for (command in commands) {
            if (command.matchCommandString(commandString)) return command
        }
        return null
    }

}