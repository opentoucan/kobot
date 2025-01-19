package uk.me.danielharman.kotlinspringbot.mappers

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.events.DiscordChannelMessageEvent
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.events.DiscordSlashCommandEvent

fun MessageReceivedEvent.toMessageEvent(): DiscordMessageEvent {
    return DiscordChannelMessageEvent(this)
}

fun SlashCommandInteractionEvent.toMessageEvent(): DiscordMessageEvent {
    return DiscordSlashCommandEvent(this)
}