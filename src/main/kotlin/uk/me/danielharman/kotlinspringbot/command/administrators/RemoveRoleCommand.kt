package uk.me.danielharman.kotlinspringbot.command.administrators

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.models.admin.enums.Role
import uk.me.danielharman.kotlinspringbot.services.admin.AdministratorService

@Component
class RemoveRoleCommand(private val administratorService: AdministratorService) : IAdminCommand {

    private val commandString = "removerole"

    override fun execute(event: PrivateMessageReceivedEvent) {
        //TODO: This is a lot duplicated code
        val thisAdmin = administratorService.getBotAdministratorByDiscordId(event.author.id)

        if (thisAdmin.failure || thisAdmin.value == null) {
            event.channel.sendMessage(Embeds.createErrorEmbed("You are not an admin.")).queue()
            return
        }

        val split = event.message.contentRaw.split(' ')

        if (split.size < 3) {
            event.channel.sendMessage(Embeds.createErrorEmbed("Not enough parameters")).queue()
            return
        }

        val user = event.jda.getUserByTag(split[1])

        if (user == null) {
            event.channel.sendMessage(Embeds.createErrorEmbed("User not found")).queue()
            return
        }

        try {
            val role = Role.valueOf(split[2])

            val addRole = administratorService.removeRole(event.author.id, user.id, role)

            if (addRole.failure) {
                event.channel.sendMessage(Embeds.createErrorEmbed(addRole.message)).queue()
                return
            }
        }
        catch (e: IllegalArgumentException){
            event.channel.sendMessage(Embeds.createErrorEmbed("No such role ${split[2]}. The current available roles are: ${Role.values().fold("") {acc, r -> "$acc $r" }}")).queue()
            return
        }

        event.channel.sendMessage(
            Embeds.infoEmbedBuilder().appendDescription("Removed ${split[2]} from ${user.asTag}").build()
        ).queue()
    }

    override fun matchCommandString(str: String): Boolean = str == commandString
    override fun getCommandString(): String = commandString
}