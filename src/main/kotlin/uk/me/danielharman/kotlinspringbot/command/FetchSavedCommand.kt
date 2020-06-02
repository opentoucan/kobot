package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.listeners.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.services.GuildService

class FetchSavedCommand(private val guildService: GuildService) : Command {
    override fun execute(event: GuildMessageReceivedEvent) {
        val guild = guildService.getGuild(event.guild.id)
        if (guild == null) {
            event.channel.sendMessage(Embeds.createErrorEmbed("Guild not found")).queue()
            return
        }
        val builder = EmbedBuilder()
                .setTitle("Saved commands")
                .setColor(0x9d03fc)


        val stringBuilder = StringBuilder()

        guild.customCommands.entries.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER, { it.key }))
                .forEach { (k, v) -> builder.addField(k, truncate(v.value, 20), true) }

        event.channel.sendMessage(builder.build()).queue()
    }

    private fun truncate(str: String, limit: Int): String =
            if (str.length <= limit) str else str.slice(IntRange(0, limit))

}