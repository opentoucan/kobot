package uk.me.danielharman.kotlinspringbot.command.interfaces

import uk.me.danielharman.kotlinspringbot.models.CommandParameter

interface ISlashCommand {
    val description: String
    val commandString: String
    val commandParameters: List<CommandParameter>
}