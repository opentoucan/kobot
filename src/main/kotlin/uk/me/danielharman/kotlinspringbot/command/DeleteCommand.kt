package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService

class DeleteCommand(private val commandService: DiscordCommandService) : Command {
    override fun execute(event: GuildMessageReceivedEvent) {

        val content = event.message.contentRaw
        val split = content.split(" ")

        if (split.size < 2) {
            event.channel.sendMessage(Embeds.createErrorEmbed("Command not found")).queue()
            return
        }

        if (commandService.deleteCommand(event.guild.id, split[1])) {
            event.channel.sendMessage(Embeds.infoEmbedBuilder().setDescription("Command deleted").build()).queue()
        } else {
            event.channel.sendMessage(Embeds.createErrorEmbed("Command not found")).queue()
        }

    }
}