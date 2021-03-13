package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.ICommand
import uk.me.danielharman.kotlinspringbot.services.RequestService

@Component
class FetchFeatureCommand(private val featureRequestService: RequestService): ICommand {

    private val commandString = listOf("feature", "request")
    private val description = "Get a feature by ID"

    override fun matchCommandString(str: String): Boolean = commandString.contains(str)

    override fun getCommandString(): String = commandString.joinToString(", ")

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) {
        val split = event.message.contentStripped.split(" ")

        if (split.size < 2)
            return

        val createRequest = featureRequestService.findById(split[1])

        val embedBuilder = EmbedBuilder()

        if (createRequest == null) {
            embedBuilder.setTitle("Error").setDescription("No such request ${split[1]}")
        } else {

            var nickname = "Unknown"
            if(!createRequest.userId.isBlank())
                nickname = event.guild.retrieveMemberById(createRequest.userId).complete()?.nickname ?: "Unknown"

            embedBuilder
                    .setTitle("Feature Request")
                    .addField("Id", createRequest.niceId, false)
                    .addField("Text", createRequest.requestText, false)
                    .addField("Created", createRequest.created.toString(), false)
                    .addField("Status", createRequest.status.name, false)
                    .addField("Requester", nickname , false)

        }
        event.channel.sendMessage(embedBuilder.build()).queue()
    }
}