package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.ICommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService
import kotlin.math.ceil

@Component
class FetchSavedCommand(
    private val springGuildService: SpringGuildService,
    private val commandService: DiscordCommandService
) : ICommand {

    private val MAX_PAGE_SIZE = 20
    private val commandString = "saved"
    private val description = "Get a list of saved commands "

    override fun matchCommandString(str: String): Boolean = str == commandString

    override fun getCommandString(): String = commandString

    override fun getCommandDescription(): String = description

    override fun execute(event: GuildMessageReceivedEvent) {

        val message = when(val getGuild = springGuildService.getGuild(event.guild.id)){
            is Failure -> Embeds.createErrorEmbed("Guild not found")
            is Success -> {
                val guild = getGuild.value
                val split = event.message.contentStripped.split(" ")
                val page = if (split.size < 2) 1 else split[1].toIntOrNull() ?: 1

                val commandCount = commandService.commandCount(guild.guildId)

                val pages = ceil((commandCount.toDouble() / MAX_PAGE_SIZE)).toInt()

                if (page < 1 || page > pages) {
                    event.channel.sendMessage(Embeds.createErrorEmbed("$page is not a valid page number, choose between 1 and $pages"))
                        .queue()
                    return
                }

                val commandList = commandService.getCommands(guild.guildId, page - 1, MAX_PAGE_SIZE)

                val builder = EmbedBuilder()
                    .setTitle("Saved commands")
                    .setDescription("Page $page of $pages ($commandCount saved commands)")
                    .setColor(0x9d03fc)

                commandList.forEach { cmd ->
                    builder.addField(cmd.key, truncate(cmd.content ?: cmd.fileName ?: "No Content", 30), true)
                }

                builder.build()
            }
        }

        event.channel.sendMessage(message).queue()
    }


    private fun truncate(str: String, limit: Int): String =
        if (str.length <= limit) str else str.slice(IntRange(0, limit))

}