package uk.me.danielharman.kotlinspringbot.factories

import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.command.interfaces.IVoiceCommand
import uk.me.danielharman.kotlinspringbot.command.voice.DefaultVoiceCommand

@Service
class VoiceCommandFactory(private val commands: List<IVoiceCommand>) {

    fun getCommand(commandString: String): IVoiceCommand {
        for (command in commands) {
            if (command.matchCommandString(commandString)) return command
        }
        return DefaultVoiceCommand(commandString)
    }

}