package uk.me.danielharman.kotlinspringbot.command.interfaces

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent

interface IAdminCommand {
    fun execute(event: PrivateMessageReceivedEvent)
    fun matchCommandString(str: String): Boolean
    fun getCommandString(): String
}