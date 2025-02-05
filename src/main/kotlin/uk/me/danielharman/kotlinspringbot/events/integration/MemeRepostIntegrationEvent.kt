package uk.me.danielharman.kotlinspringbot.events.integration

import kotlinx.serialization.Serializable

@Serializable
data class MemeRepostIntegrationEvent(
    val replyImage: String,
    val guildId: String,
    val channelId: String,
    val links: List<Link> = listOf()
)

@Serializable
data class Link(
    val guildId: String,
    val channelId: String,
    val messageId: String,
    val score: Float
)
