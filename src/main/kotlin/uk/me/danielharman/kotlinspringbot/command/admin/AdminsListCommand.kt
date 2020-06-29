package uk.me.danielharman.kotlinspringbot.command.admin

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.command.Command
import uk.me.danielharman.kotlinspringbot.listeners.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.services.GuildService

class AdminsListCommand(val guildService: GuildService, private val primaryAdminUserId: String) : Command {
    override fun execute(event: GuildMessageReceivedEvent) {
        event.channel.sendMessage(createAdminUsersEmbed(event)).queue()
    }

    private fun createAdminUsersEmbed(message: GuildMessageReceivedEvent): MessageEmbed {

        val guildName = message.guild.name
        val guild = guildService.getGuild(message.guild.id)

        return if (guild == null) {
            Embeds.createErrorEmbed("Could not find data for $guildName")
        } else {

            val stringBuilder = StringBuilder()

            stringBuilder.append("Bot controller:  ${message.jda.getUserById(primaryAdminUserId)?.asTag ?: primaryAdminUserId}\n")

            guild.privilegedUsers.forEach { s ->
                stringBuilder.append(message.jda.getUserById(s)?.asTag ?: s)
            }

            EmbedBuilder()
                    .appendDescription(stringBuilder.toString())
                    .setColor(0x9d03fc)
                    .setTitle("Admins for $guildName")
                    .build()
        }

    }

}