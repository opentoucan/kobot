package uk.me.danielharman.kotlinspringbot.actors.discord.messages

data class DiscordChannelMessage(val msg: String, val guildId: String, val channelId: String)