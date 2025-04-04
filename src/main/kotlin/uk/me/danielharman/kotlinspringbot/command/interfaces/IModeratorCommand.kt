package uk.me.danielharman.kotlinspringbot.command.interfaces

import net.dv8tion.jda.api.events.message.MessageReceivedEvent

interface IModeratorCommand {
    fun execute(event: MessageReceivedEvent)

    fun matchCommandString(str: String): Boolean

    fun getCommandString(): String
}
