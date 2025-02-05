package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.helpers.Comparators
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

@Component
class WordCountCommand(private val springGuildService: SpringGuildService) :
    Command("wordcounts", "List member word counts"), ISlashCommand {

    override fun execute(event: DiscordMessageEvent) {
        if (event.guild == null) {
            event.reply(Embeds.createErrorEmbed("This command can only be used in Servers"))
            return
        }

        val guildId = event.guild.id
        val guildName = event.guild.name

        val message =
            when (val getSpringGuild = springGuildService.getGuild(guildId)) {
                is Failure ->
                    EmbedBuilder()
                        .addField("error", "Could not find stats for server", false)
                        .build()
                is Success -> {
                    val stringBuilder = StringBuilder()

                    getSpringGuild.value.userWordCounts.entries
                        .stream()
                        .sorted(Comparators.mapStrIntComparator)
                        .limit(20)
                        .forEach { (s, i) ->
                            run {
                                try {
                                    stringBuilder.append(
                                        "${
                                        event.guild?.retrieveMemberById(s)?.complete()?.nickname ?: s
                                    } - $i words\n")
                                } catch (e: ErrorResponseException) {
                                    logger.error("Failed to find user $s by id")
                                }
                            }
                        }

                    EmbedBuilder()
                        .appendDescription(stringBuilder.toString())
                        .setColor(0x9d03fc)
                        .setTitle("Words said per user for $guildName")
                        .build()
                }
            }
        event.reply(message)
    }
}
