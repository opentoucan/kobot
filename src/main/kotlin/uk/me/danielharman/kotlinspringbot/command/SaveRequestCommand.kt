package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.ICommand
import uk.me.danielharman.kotlinspringbot.services.RequestService

@Component
class SaveRequestCommand(private val featureRequestService: RequestService) : ICommand {

    private val commandString = listOf("newrequest", "newfeature")
    private val description = "Save a new command"

    override fun matchCommandString(str: String): Boolean = commandString.contains(str)

    override fun getCommandString(): String = commandString.joinToString(", ")

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) {

        val content = event.message.contentRaw
        val split = content.split(" ")
        if (split.size < 2) {
            event.message.channel.sendMessage("Enter something!").queue()
            return
        }

        val createRequest = featureRequestService.createRequest(split.subList(1, split.size)
                .joinToString(" "), event.author.id)

        event.channel.sendMessage("Created request: ${createRequest.niceId}").queue()
    }

}
