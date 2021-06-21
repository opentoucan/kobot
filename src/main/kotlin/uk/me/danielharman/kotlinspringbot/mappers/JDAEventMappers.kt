package uk.me.danielharman.kotlinspringbot.mappers

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.messages.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.messages.DiscordMessageEvent.Type.ChannelMessage
import uk.me.danielharman.kotlinspringbot.messages.DiscordMessageEvent.Type.ChannelSlashCommand
import uk.me.danielharman.kotlinspringbot.messages.MessageEvent

fun GuildMessageReceivedEvent.toMessageEvent(): DiscordMessageEvent {

    return DiscordMessageEvent(
        this.message.contentStripped,
        this.channel,
        ChannelMessage,
        this.author,
        this.guild,
        this.message.attachments,
        this.message.mentionedUsers
    )

}

fun SlashCommandEvent.toMessageEvent(): DiscordMessageEvent {
    return DiscordMessageEvent("", this.channel, ChannelSlashCommand, this.user, this.guild, listOf(), listOf())
}