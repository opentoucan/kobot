package uk.me.danielharman.kotlinspringbot.commands

import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.command.administrators.DefaultAdminCommand
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.IAdminCommand
import uk.me.danielharman.kotlinspringbot.command.interfaces.IModeratorCommand
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.command.moderators.DefaultModeratorCommand
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.properties.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService
import uk.me.danielharman.kotlinspringbot.services.admin.AdministratorService
import java.util.Locale

enum class CommandPermission {
    PUBLIC,
    GUILD_MODERATOR,
    BOT_ADMIN,
}

@Service
class CommandRegistry(
    private val publicCommandProvider: ObjectProvider<Command>,
    private val moderatorCommandProvider: ObjectProvider<IModeratorCommand>,
    private val adminCommandProvider: ObjectProvider<IAdminCommand>,
    private val slashCommandProvider: ObjectProvider<ISlashCommand>,
    private val springGuildService: SpringGuildService,
    private val administratorServiceProvider: ObjectProvider<AdministratorService>,
    private val properties: KotlinBotProperties,
) {
    private val publicCommands by lazy {
        index(
            publicCommandProvider.orderedStream().toList(),
            Command::commandString,
            CommandPermission.PUBLIC,
        )
    }

    private val moderatorCommands by lazy {
        index(
            moderatorCommandProvider.orderedStream().toList(),
            IModeratorCommand::getCommandString,
            CommandPermission.GUILD_MODERATOR,
        )
    }

    private val adminCommands by lazy {
        index(
            adminCommandProvider.orderedStream().toList(),
            IAdminCommand::getCommandString,
            CommandPermission.BOT_ADMIN,
        )
    }

    fun publicCommand(name: String): Command? = publicCommands[normalize(name)]

    fun moderatorCommand(name: String): IModeratorCommand =
        moderatorCommands[normalize(name)] ?: DefaultModeratorCommand(name)

    fun adminCommand(name: String): IAdminCommand = adminCommands[normalize(name)] ?: DefaultAdminCommand(name)

    fun slashCommands(): List<ISlashCommand> = slashCommandProvider.orderedStream().toList()

    fun isAuthorized(
        permission: CommandPermission,
        userId: String,
        guildId: String? = null,
    ): Boolean = when (permission) {
        CommandPermission.PUBLIC -> true
        CommandPermission.GUILD_MODERATOR -> {
            val currentGuildId = guildId ?: return false
            userId == properties.primaryPrivilegedUserId ||
                springGuildService.isModerator(currentGuildId, userId) is Success
        }

        CommandPermission.BOT_ADMIN ->
            administratorServiceProvider.getObject().getBotAdministratorByDiscordId(userId) is Success
    }

    private fun <T> index(
        commands: List<T>,
        name: (T) -> String,
        permission: CommandPermission,
    ): Map<String, T> {
        val indexed = linkedMapOf<String, T>()
        commands.forEach { command ->
            val normalizedName = normalize(name(command))
            require(normalizedName.isNotBlank()) { "${permission.name} command name must not be blank" }
            require(indexed.putIfAbsent(normalizedName, command) == null) {
                "Duplicate ${permission.name} command '$normalizedName'"
            }
        }
        return indexed
    }

    private fun normalize(name: String): String = name.trim().lowercase(Locale.ROOT)
}