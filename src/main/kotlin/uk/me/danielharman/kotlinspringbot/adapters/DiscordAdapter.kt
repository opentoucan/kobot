package uk.me.danielharman.kotlinspringbot.adapters

import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.actors.ActorProvider
import uk.me.danielharman.kotlinspringbot.actors.discord.messages.DiscordChannelEmbedMessage

@Component
class DiscordAdapter(private val actorProvider: ActorProvider) {

    fun sendMessage(channelId: String, message: String) {
        actorProvider.getActor("discord-actor") ?: return
    }

    fun sendMessage(channelId: String, embedMessage: DiscordChannelEmbedMessage) {

    }

}