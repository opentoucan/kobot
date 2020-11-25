package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.helpers.Comparators
import uk.me.danielharman.kotlinspringbot.services.GuildService

class UserStatsCommand (private val guildService: GuildService): Command{
    override fun execute(event: GuildMessageReceivedEvent) {

        val guildId = event.message.guild.id
        val guildName = event.message.guild.name
        val springGuild = guildService.getGuild(guildId)

        val message = if (springGuild == null) {
            EmbedBuilder().addField("error", "Could not find stats for server", false).build()
        } else {

            val stringBuilder = StringBuilder()

            springGuild.userWordCounts.entries
                    .stream()
                    .sorted(Comparators.mapStrIntComparator)
                    .limit(20)
                    .forEach { (s, i) ->
                        stringBuilder.append("${event.message.guild.retrieveMemberById(s).complete()?.nickname ?: s} - $i words\n")
                    }

            EmbedBuilder()
                    .appendDescription(stringBuilder.toString())
                    .setColor(0x9d03fc)
                    .setTitle("Words said per user for $guildName")
                    .build()
        }

        event.channel.sendMessage(message).queue()
    }
}