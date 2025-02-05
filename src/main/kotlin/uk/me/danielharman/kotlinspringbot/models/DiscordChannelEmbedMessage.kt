package uk.me.danielharman.kotlinspringbot.models

import net.dv8tion.jda.api.entities.MessageEmbed

data class DiscordChannelEmbedMessage(val msg: MessageEmbed, val channelId: String)
