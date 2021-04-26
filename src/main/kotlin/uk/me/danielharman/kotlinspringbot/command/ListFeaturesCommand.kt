package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.ICommand
import uk.me.danielharman.kotlinspringbot.services.RequestService

@Component
class ListFeaturesCommand(private val featureRequestService: RequestService): ICommand {

    private val commandString = listOf("features", "requests")
    private val description = "List feature requests"

    override fun matchCommandString(str: String): Boolean = commandString.contains(str)

    override fun getCommandString(): String = commandString.joinToString(", ")

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) = event.channel.sendMessage(EmbedBuilder().setDescription(
            featureRequestService.getRequests().fold("") { acc, s -> "$acc ${s.niceId}," }).build()).queue()
}