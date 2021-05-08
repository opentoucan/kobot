package uk.me.danielharman.kotlinspringbot.command.administrators

import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.services.admin.AdministratorService

@Component
class ListAdminCommand(private val administratorService: AdministratorService) : IAdminCommand {

    private val commandString = "admins"

    override fun execute(event: PrivateMessageReceivedEvent) {
        when (val thisAdmin = administratorService.getBotAdministratorByDiscordId(event.author.id)) {
            is Failure -> event.channel.sendMessage(Embeds.createErrorEmbed("You are not an admin.")).queue()
            is Success -> {
                when (val admins = administratorService.getAdministrators(thisAdmin.value.id)) {
                    is Failure -> Embeds.createErrorEmbed(admins.reason)
                    is Success -> {
                        val infoEmbedBuilder = Embeds.infoEmbedBuilder()
                        //TODO: Pretty up
                        for (admin in admins.value) {
                            infoEmbedBuilder.appendDescription("${admin.discordId}\n")
                        }
                        event.channel.sendMessage(infoEmbedBuilder.build())
                    }
                }
            }
        }
    }
    override fun matchCommandString(str: String): Boolean = str == commandString
    override fun getCommandString(): String = commandString
}