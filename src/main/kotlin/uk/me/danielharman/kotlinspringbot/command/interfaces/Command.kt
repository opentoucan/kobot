package uk.me.danielharman.kotlinspringbot.command.interfaces

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.events.MessageEvent
import uk.me.danielharman.kotlinspringbot.models.CommandParameter

abstract class Command(val commandString: String, val description: String, val commandParameters: List<CommandParameter> = listOf()) {

    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)

    abstract fun execute(event: DiscordMessageEvent)

    open fun execute(event: MessageEvent){
        when(event){
            is DiscordMessageEvent -> execute(event)
        }
    }

    open fun matchCommandString(str: String): Boolean = commandString == str
}