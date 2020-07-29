package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.services.RequestService

class SaveRequestCommand(val featureRequestService: RequestService) : Command {
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
