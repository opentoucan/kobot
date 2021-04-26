package uk.me.danielharman.kotlinspringbot.command.administrators

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.OperationHelpers
import uk.me.danielharman.kotlinspringbot.models.admin.enums.Role
import uk.me.danielharman.kotlinspringbot.services.admin.AdministratorService

@Component
class InspectRolesCommand(private val administratorService: AdministratorService) : IAdminCommand {

    private val commandString = "inspectroles"

    override fun execute(event: PrivateMessageReceivedEvent) {

        val thisAdmin = administratorService.getBotAdministratorByDiscordId(event.author.id)

        if (thisAdmin.failure || thisAdmin.value == null) {
            event.channel.sendMessage(Embeds.createErrorEmbed("You are not an admin.")).queue()
            return
        }

        val split = event.message.contentRaw.split(' ')

        if (split.size < 2) {
            event.channel.sendMessage(Embeds.createErrorEmbed("Not enough parameters")).queue()
            return
        }

        val user = event.jda.getUserByTag(split[1])

        if (user == null) {
            event.channel.sendMessage(Embeds.createErrorEmbed("User not found")).queue()
            return
        }

        val roles: OperationHelpers.OperationResult<Set<Role>?> = administratorService.getRoles(event.author.id, user.id)

        if(roles.failure)
        {
            event.channel.sendMessage(Embeds.createErrorEmbed(roles.message)).queue()
            return
        }

        event.channel.sendMessage(
            Embeds.infoEmbedBuilder().appendDescription("${roles.value}").build()
        ).queue()
    }

    override fun matchCommandString(str: String): Boolean = str == commandString
    override fun getCommandString(): String = commandString
}