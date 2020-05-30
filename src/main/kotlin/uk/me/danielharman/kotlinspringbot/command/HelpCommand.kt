package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.listeners.helpers.Embeds

class HelpCommand(private val commandPrefix: String): Command {
    override fun execute(event: GuildMessageReceivedEvent) = event.channel.sendMessage(Embeds.createHelpEmbed(commandPrefix)).queue()
}