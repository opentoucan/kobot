package uk.me.danielharman.kotlinspringbot.models


data class DiscordChannelMessage(val msg: String, val guildId: String, val channelId: String, val attachments: List<DiscordChannelAttachment> = listOf())
data class DiscordChannelAttachment(val fileName: String, val content: ByteArray)