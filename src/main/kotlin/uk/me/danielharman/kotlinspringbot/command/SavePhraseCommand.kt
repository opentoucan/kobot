package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.services.GuildService

class SavePhraseCommand(private val guildService: GuildService): Command {
    override fun execute(event: GuildMessageReceivedEvent) {
        val content = event.message.contentRaw
        val split = content.split(" ")

        if (split.size < 3) {
            event.message.channel.sendMessage("Phrase missing").queue()
            return
        }

        if (split[1].contains(Regex("[_.!,?$\\\\-]"))) {
            event.message.channel.sendMessage("Cannot save with that phrase").queue()
            return
        }

        guildService.saveCommand(event.message.guild.id, split[1], split.subList(2, split.size).joinToString(" "))
        event.message.channel.sendMessage("Saved!").queue()
    }
}