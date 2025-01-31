package uk.me.danielharman.kotlinspringbot.events.integration

import kotlinx.serialization.Serializable

@Serializable
data class MemePostedIntegrationEvent(
    val sender: String,
    val nameColour: String,
    val avatar: String,
    val meme: String,
    val guildId: String,
    val channelId: String,
    val messageId: String)
