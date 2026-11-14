package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.helpers.Comparators
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.PURPLE
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

private const val LIST_OF_USERS_LIMIT = 20

@Component
class WordCountCommand(private val springGuildService: SpringGuildService) :
    Command("wordcounts", "List member word counts"),
    ISlashCommand {

    override fun execute(event: DiscordMessageEvent) {
        if (event.guild == null) {
            event.reply(Embeds.createErrorEmbed("This command can only be used in Servers"))
            return
        }

        val guild = event.guild
        val guildId = guild.id
        val guildName = guild.name

        val message = when (val getSpringGuild = springGuildService.getGuild(guildId)) {
            is Failure -> EmbedBuilder().addField("error", "Could not find stats for server", false).build()

            is Success -> {
                val wordCounts = getSpringGuild.value.userWordCounts.entries
                    .sortedWith(Comparators.mapStrIntComparator)
                    .take(LIST_OF_USERS_LIMIT)
                    .joinToString(separator = "\n") { (userId, count) ->
                        "${guild.getMemberById(userId)?.effectiveName ?: userId} - $count words"
                    }

                EmbedBuilder()
                    .appendDescription(wordCounts)
                    .setColor(PURPLE)
                    .setTitle("Words said per user for $guildName")
                    .build()
            }
        }
        event.reply(message)
    }
}
