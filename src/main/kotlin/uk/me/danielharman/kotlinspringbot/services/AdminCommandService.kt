package uk.me.danielharman.kotlinspringbot.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.command.*
import uk.me.danielharman.kotlinspringbot.command.admin.*

@Service
class AdminCommandService(val guildService: GuildService) {

    @Value("\${discord.primaryPrivilegedUserId}")
    private lateinit var primaryAdminId: String

    fun getCommand(command: String): Command {
        return when (command) {
            "ping" -> PingCommand()
            "addadmin" -> AddAdminCommand(guildService)
            "removeadmin" -> RemoveAdminCommand(guildService)
            "admins" -> AdminsListCommand(guildService, primaryAdminId)
            "purge" -> PurgeMessagesCommand()
            else -> DefaultCommand(command);
        }
    }

}