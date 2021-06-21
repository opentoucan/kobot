package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.helpers.Comparators
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.messages.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

@Component
class WordCountCommand(private val springGuildService: SpringGuildService) :
    Command("wordcounts", "List member word counts") {

    fun execute(event: GuildMessageReceivedEvent) {

        val guildId = event.message.guild.id
        val guildName = event.message.guild.name

        val message = when (val getSpringGuild = springGuildService.getGuild(guildId)) {
            is Failure -> EmbedBuilder().addField("error", "Could not find stats for server", false).build()
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
                                        event.message.guild.retrieveMemberById(s).complete()?.nickname ?: s
                                    } - $i words\n"
                                )
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
        event.channel.sendMessage(message).queue()
    }

    override fun execute(event: DiscordMessageEvent) {
        TODO("Not yet implemented")
    }
}