package uk.me.danielharman.kotlinspringbot.listeners

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.hooks.ListenerAdapter
import uk.me.danielharman.kotlinspringbot.listeners.helpers.Embeds.createErrorEmbed
import uk.me.danielharman.kotlinspringbot.services.GuildService
import uk.me.danielharman.kotlinspringbot.services.RequestService

class AdminMessageListener(private val guildService: GuildService,
                           private val adminPrefix: String, private val primaryAdminUserId: String,
                           private val featureRequestService: RequestService) : ListenerAdapter() {

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {

        val author = event.author
        val message = event.message

        //Never parse a bot message
        if (author.isBot)
            return

        if (message.contentStripped.startsWith(adminPrefix)) {

            val cmd = event.message.contentStripped.split(" ")[0].removePrefix(adminPrefix)
            val channel = event.channel

            if (event.author.id != primaryAdminUserId
                    && !guildService.isPrivileged(event.guild.id, event.author.id)) {
                channel.sendMessage("You are not an admin!").queue()
                return
            }

            when (cmd) {
                "ping" -> channel.sendMessage("pong").queue()
                "addadmin" -> addAdmin(event)
                "removeadmin" -> removeAdmin(event)
                "admins" -> channel.sendMessage(createAdminUsersEmbed(event)).queue()
                "purge" -> purgeMessagesPrivileged(event)
                //"disconnect" -> disconnect(event)
                else -> channel.sendMessage("No such command $cmd").queue()
            }
        }

    }

    private fun purgeMessagesPrivileged(event: GuildMessageReceivedEvent) {

        val s = event.message.contentStripped.split(" ")

        if (s.size < 2) {
            event.channel.sendMessage("Number of to delete messages not given.").queue()
            return
        }

        val number = s[1].toInt()

        if (number > 50) {
            event.channel.sendMessage("Careful now!").queue()
            return
        }

        val messages = event.channel.history.retrievePast(number).complete()

        try {
           event.channel.purgeMessages(messages)
        } catch (e: InsufficientPermissionException) {
            event.channel.sendMessage("I don't have permissions to delete messages!").queue()
            return
        }

        event.channel.sendMessage("https://cdn.discordapp.com/attachments/554379034750877707/650988065539620874/giphy_1.gif").queue()
    }

    private fun addAdmin(message: GuildMessageReceivedEvent) {
        message.message.mentionedUsers.forEach { u -> guildService.addPrivileged(message.guild.id, u.id) }
        message.channel.sendMessage("Added ${message.message.mentionedUsers}").queue()
    }

    private fun removeAdmin(message: GuildMessageReceivedEvent) {
        message.message.mentionedUsers.forEach { u -> guildService.removedPrivileged(message.guild.id, u.id) }
        message.channel.sendMessage("Removed ${message.message.mentionedUsers}").queue()
    }

    private fun createAdminUsersEmbed(message: GuildMessageReceivedEvent): MessageEmbed {

        val guildName = message.guild.name
        val guild = guildService.getGuild(message.guild.id)

        return if (guild == null) {
            createErrorEmbed("Could not find data for $guildName")
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