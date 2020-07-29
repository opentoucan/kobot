package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.services.GuildService
import kotlin.math.min

class FetchSavedCommand(private val guildService: GuildService) : Command {

    private val MAXPAGE = 20

    override fun execute(event: GuildMessageReceivedEvent) {
        val guild = guildService.getGuild(event.guild.id)
        if (guild == null) {
            event.channel.sendMessage(Embeds.createErrorEmbed("Guild not found")).queue()
            return
        }

        val split = event.message.contentStripped.split(" ")

        var commandList  = guild.customCommands.entries.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER, { it.key }))
        val noOfPages = commandList.size / MAXPAGE + 1
        val pageSelector = min(if(split.size < 2) 1 else split[1].toIntOrNull() ?: 1, noOfPages)
        val listSize = commandList.size

        if (commandList.size > 20) {
            val (i, j) = getPageRange(commandList.size, MAXPAGE, pageSelector)
            commandList = commandList.subList(i, j)
        }

        val builder = EmbedBuilder()
                .setTitle("Saved commands")
                .setDescription("Page $pageSelector of $noOfPages ($listSize saved commands)")
                .setColor(0x9d03fc)

        commandList.forEach { (k, v) -> builder.addField(k, truncate(v.value, 30), true) }

        event.channel.sendMessage(builder.build()).queue()
    }

    private fun getPageRange(maxSize: Int, pageSize: Int, page: Int ): Pair<Int, Int> {
        val startIndex = pageSize * (page - 1)
        val endIndex = min(startIndex + pageSize, maxSize)
        return Pair(startIndex, endIndex)
    }

    private fun truncate(str: String, limit: Int): String =
            if (str.length <= limit) str else str.slice(IntRange(0, limit))

}