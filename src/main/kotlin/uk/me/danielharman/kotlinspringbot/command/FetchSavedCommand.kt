package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService
import kotlin.math.ceil

@Component
class FetchSavedCommand(
    private val springGuildService: SpringGuildService,
    private val commandService: DiscordCommandService
) : Command("saved",
    "Get a list of saved commands",
    listOf(CommandParameter(0, "Page", CommandParameter.ParamType.Long, "Page number to view", false))), ISlashCommand {

    private val MAX_PAGE_SIZE = 20

    private fun truncate(str: String, limit: Int): String =
        if (str.length <= limit) str else str.slice(IntRange(0, limit))

    override fun execute(event: DiscordMessageEvent) {
        if (event.guild == null) {
            event.reply(Embeds.createErrorEmbed("This command can only be used in Servers"))
            return
        }

        val message = when (val getGuild = springGuildService.getGuild(event.guild.id)) {
            is Failure -> Embeds.createErrorEmbed("Guild not found")
            is Success -> {

                val guild = getGuild.value

                val paramValue = event.getParamValue(commandParameters[0])
                val page = paramValue.asLong() ?: 1L

                when (val commandCount = commandService.commandCount(guild.guildId)) {
                    is Failure -> Embeds.createErrorEmbed(commandCount.reason)
                    is Success -> {

                        val pages = ceil((commandCount.value.toDouble() / MAX_PAGE_SIZE)).toInt()

                        if (page < 1 || page > pages) {
                            event.reply(Embeds.createErrorEmbed("$page is not a valid page number, choose between 1 and $pages"))
                            return
                        }

                        when (val commandList = commandService.getCommands(guild.guildId,
                            (page - 1).toInt(), MAX_PAGE_SIZE)) {
                            is Failure -> Embeds.createErrorEmbed(commandList.reason)
                            is Success -> {
                                val builder = EmbedBuilder()
                                    .setTitle("Saved commands")
                                    .setDescription("Page $page of $pages (${commandCount.value} saved commands)")
                                    .setColor(0x9d03fc)

                                commandList.value.forEach { cmd ->
                                    builder.addField(
                                        cmd.key,
                                        truncate(cmd.content ?: cmd.fileName ?: "No Content", 30),
                                        true
                                    )
                                }

                                builder.build()
                            }
                        }
                    }
                }
            }
        }

        event.reply(message)
    }

}