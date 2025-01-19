package uk.me.danielharman.kotlinspringbot.command.administrators

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.services.admin.AdministratorService

@Component
class InspectRolesCommand(private val administratorService: AdministratorService) : IAdminCommand {

    private val commandString = "inspectroles"

    override fun execute(event: MessageReceivedEvent) {

        when (administratorService.getBotAdministratorByDiscordId(event.author.id)) {
            is Failure -> {
                event.channel.sendMessageEmbeds(Embeds.createErrorEmbed("You are not an admin.")).queue()
            }
            is Success -> {
                val split = event.message.contentRaw.split(' ')

                if (split.size < 2) {
                    event.channel.sendMessageEmbeds(Embeds.createErrorEmbed("Not enough parameters")).queue()
                    return
                }

                val user = event.jda.getUserByTag(split[1])

                if (user == null) {
                    event.channel.sendMessageEmbeds(Embeds.createErrorEmbed("User not found")).queue()
                    return
                }

                when (val roles = administratorService.getRoles(event.author.id, user.id)) {
                    is Failure -> event.channel.sendMessageEmbeds(Embeds.createErrorEmbed(roles.reason)).queue()
                    is Success -> event.channel.sendMessageEmbeds(
                        Embeds.infoEmbedBuilder().appendDescription("${roles.value}").build()
                    ).queue()
                }
            }
        }
    }

    override fun matchCommandString(str: String): Boolean = str == commandString
    override fun getCommandString(): String = commandString
}