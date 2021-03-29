package uk.me.danielharman.kotlinspringbot.command.administrators

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.OperationHelpers
import uk.me.danielharman.kotlinspringbot.models.admin.Administrator
import uk.me.danielharman.kotlinspringbot.models.admin.enums.Role
import uk.me.danielharman.kotlinspringbot.services.admin.AdministratorService

@Component
class ListAdminCommand(private val administratorService: AdministratorService) : IAdminCommand {

    private val commandString = "admins"

    override fun execute(event: PrivateMessageReceivedEvent) {

        val thisAdmin = administratorService.getBotAdministratorByDiscordId(event.author.id)

        if (thisAdmin.failure || thisAdmin.value == null) {
            event.channel.sendMessage(Embeds.createErrorEmbed("You are not an admin.")).queue()
            return
        }

        val admins = administratorService.getAdministrators(thisAdmin.value.id)

        val infoEmbedBuilder = Embeds.infoEmbedBuilder()
        //TODO: Pretty up
        for(admin in admins.value!!){
            infoEmbedBuilder.appendDescription("${admin.discordId}\n")
        }

        event.channel.sendMessage(infoEmbedBuilder.build()).queue()
    }

    override fun matchCommandString(str: String): Boolean = str == commandString
    override fun getCommandString(): String = commandString
}