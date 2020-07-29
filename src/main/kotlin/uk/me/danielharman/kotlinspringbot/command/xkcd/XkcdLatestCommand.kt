package uk.me.danielharman.kotlinspringbot.command.xkcd

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.command.Command
import uk.me.danielharman.kotlinspringbot.helpers.Embeds.createXkcdComicEmbed
import uk.me.danielharman.kotlinspringbot.services.XkcdService


class XkcdLatestCommand(private val xkcdService: XkcdService) : Command {
    override fun execute(event: GuildMessageReceivedEvent) {

    }
}