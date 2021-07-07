package uk.me.danielharman.kotlinspringbot.factories

import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.command.SendCustomCommand
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.services.AttachmentService
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

@Service
class CommandFactory(
    private val commands: List<Command>,
    private val springGuildService: SpringGuildService,
    private val attachmentService: AttachmentService,
    private val commandService: DiscordCommandService
) {

    fun getCommand(commandString: String): Command {
        for (command in commands) {
            if (command.matchCommandString(commandString)) return command
        }
        return SendCustomCommand(springGuildService, attachmentService, commandService, commandString)
    }

}