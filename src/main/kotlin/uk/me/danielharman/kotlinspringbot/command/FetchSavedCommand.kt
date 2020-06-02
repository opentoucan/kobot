package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.listeners.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.services.GuildService

class FetchSavedCommand(private val guildService: GuildService): Command {
    override fun execute(event: GuildMessageReceivedEvent) {
        val guild = guildService.getGuild(event.guild.id)
        if(guild == null) {
            event.channel.sendMessage(Embeds.createErrorEmbed("Guild not found")).queue()
            return
        }
        val stringBuilder = StringBuilder()
        guild.customCommands.entries.forEach { (s, _) -> stringBuilder.append("$s ") }

        event.channel.sendMessage(EmbedBuilder()
                .setTitle("Saved commands")
                .setColor(0x9d03fc)
                .setDescription(stringBuilder.toString())
                .build()).queue()
    }
}