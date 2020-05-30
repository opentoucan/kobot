package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.services.RequestService

class ListFeaturesCommand(private val featureRequestService: RequestService): Command {
    override fun execute(event: GuildMessageReceivedEvent) = event.channel.sendMessage(EmbedBuilder().setDescription(
            featureRequestService.getRequests().fold("") { acc, s -> "$acc ${s.niceId}," }).build()).queue()
}