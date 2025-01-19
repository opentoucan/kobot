package uk.me.danielharman.kotlinspringbot.command.administrators

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.services.admin.AdministratorService

@Component
class AddAdminCommand(private val administratorService: AdministratorService) : IAdminCommand {

    private val commandString = "addadmin"

    override fun execute(event: MessageReceivedEvent) {

        when (val thisAdmin = administratorService.getBotAdministratorByDiscordId(event.author.id)){
            is Failure -> event.channel.sendMessageEmbeds(Embeds.createErrorEmbed("You are not an admin.")).queue()
            is Success -> {
                val split = event.message.contentRaw.split(' ')

                if (split.size < 2) {
                    event.channel.sendMessageEmbeds(Embeds.createErrorEmbed("You are not an admin.")).queue()
                    return
                }

                val user = event.jda.getUserByTag(split[1])

                if (user == null) {
                    event.channel.sendMessageEmbeds(Embeds.createErrorEmbed("User not found")).queue()
                    return
                }

                val createBotAdministrator =
                    administratorService.createBotAdministrator(thisAdmin.value.id, user.id, setOf())

                if (createBotAdministrator is Failure) {
                    event.channel.sendMessageEmbeds(Embeds.createErrorEmbed(createBotAdministrator.reason)).queue()
                    return
                }

                event.channel.sendMessageEmbeds(
                    Embeds.infoEmbedBuilder().appendDescription("Added ${user.asTag} as an admin").build()
                ).queue()
            }
        }
    }

    override fun matchCommandString(str: String): Boolean = str == commandString
    override fun getCommandString(): String = commandString
}