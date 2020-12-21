package uk.me.danielharman.kotlinspringbot.actors.discord.messages

import net.dv8tion.jda.api.entities.MessageEmbed

data class DiscordChannelEmbedMessage(val msg: MessageEmbed, val channelId: String)