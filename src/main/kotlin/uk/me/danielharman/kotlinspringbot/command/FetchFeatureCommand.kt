package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.services.RequestService

class FetchFeatureCommand(private val featureRequestService: RequestService): Command {
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
                nickname = event.guild.getMemberById(createRequest.userId)?.nickname ?: "Unknown"

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