package uk.me.danielharman.kotlinspringbot.services.admin

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.helpers.flatMap
import uk.me.danielharman.kotlinspringbot.helpers.map
import uk.me.danielharman.kotlinspringbot.models.SpringGuild
import uk.me.danielharman.kotlinspringbot.models.admin.Administrator
import uk.me.danielharman.kotlinspringbot.models.admin.enums.Role
import uk.me.danielharman.kotlinspringbot.objects.ApplicationInfo
import uk.me.danielharman.kotlinspringbot.properties.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.repositories.admin.AdministratorRepository
import uk.me.danielharman.kotlinspringbot.services.DiscordService
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService
import java.time.LocalDateTime

@Service
class AdministratorService(
    private val repository: AdministratorRepository,
    private val springGuildService: SpringGuildService,
    private val discordService: DiscordService,
    private val properties: KotlinBotProperties,
    private val mongoOperations: MongoOperations,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun getBotVersion(): OperationResult<String, String> = Success(ApplicationInfo.version)

    fun getBotStartTime(): OperationResult<LocalDateTime, String> = Success(ApplicationInfo.startTime)

    fun getDiscordStartTime(): OperationResult<LocalDateTime, String> = discordService.getDiscordStartTime()

    fun getBotDiscordName(): OperationResult<String, String> = discordService.getBotName()

    fun closeDiscordConnection(userId: String): OperationResult<String, String> = getBotAdministratorById(userId).flatMap { discordService.closeDiscordConnection() }

    fun startDiscordConnection(userId: String): OperationResult<String, String> = getBotAdministratorById(userId).flatMap { discordService.startDiscordConnection() }

    fun restartDiscordConnection(userId: String): OperationResult<String, String> = closeDiscordConnection(userId).flatMap { startDiscordConnection(userId) }

    fun getBotAdministratorById(id: String): OperationResult<Administrator, String> {
        val administrator = repository.findById(id)
        if (administrator.isEmpty) return Failure("Administrator not found")
        return Success(administrator.get())
    }

    fun getBotAdministratorByDiscordId(id: String): OperationResult<Administrator, String> {
        val administrator =
            repository.getByDiscordId(id) ?: return Failure("Administrator not found")
        return Success(administrator)
    }

    fun createBotAdministrator(
        discordId: String,
        roles: Set<Role> = setOf(),
    ): OperationResult<Administrator, String> = when (val admin = getBotAdministratorByDiscordId(discordId)) {
        is Failure -> Success(repository.save(Administrator(discordId, roles)))
        is Success -> admin
    }

    fun createBotAdministrator(
        userId: String,
        discordId: String,
        roles: Set<Role>,
    ): OperationResult<Administrator, String> {
        val permission = hasRoles(userId, Role.ManageAdmin)
        if (permission is Failure) return permission

        val administrator = repository.save(Administrator(discordId, roles))
        logToAdmins("${administrator.discordId} added as admin by $userId")
        return Success(administrator)
    }

    fun removeBotAdministrator(
        userId: String,
        discordId: String,
    ): OperationResult<String, String> {
        val permission = hasRoles(userId, Role.ManageAdmin)
        if (permission is Failure) return permission

        if (discordId == properties.primaryPrivilegedUserId) {
            return Failure("Cannot remove primary admin")
        }
        repository.deleteByDiscordId(discordId)
        logToAdmins("$discordId removed as admin by $userId")
        return Success("Deleted")
    }

    fun addSpringGuildAdministrator(
        userId: String,
        guildId: String,
        newAdminId: String,
    ): OperationResult<String, String> = springGuildService.getGuild(guildId).flatMap { guild ->
        if (guild.privilegedUsers.contains(userId)) {
            springGuildService.addModerator(guildId, newAdminId)
        } else {
            Failure("Insufficient permissions.")
        }
    }

    fun removeSpringGuildAdministrator(
        userId: String,
        guildId: String,
        adminId: String,
    ): OperationResult<String, String> = springGuildService.getGuild(guildId).flatMap { guild ->
        if (guild.privilegedUsers.contains(userId)) {
            springGuildService.removeModerator(guildId, adminId)
        } else {
            Failure("Insufficient permissions.")
        }
    }

    fun deleteSpringGuild(
        userId: String,
        guildId: String,
    ): OperationResult<String, String> = springGuildService.getGuild(guildId).flatMap { guild ->
        if (guild.privilegedUsers.contains(userId)) {
            springGuildService.deleteSpringGuild(guildId)
        } else {
            Failure("Insufficient permissions.")
        }
    }

    fun getSpringGuild(
        userId: String,
        guildId: String,
    ): OperationResult<SpringGuild, String> = springGuildService.getGuild(guildId).flatMap { guild ->
        if (guild.privilegedUsers.contains(userId)) {
            Success(guild)
        } else {
            Failure("Insufficient permissions.")
        }
    }

    fun syncGuildAdmins(): OperationResult<String, String> = springGuildService.getGuildsWithoutModerators().map { guildsWithoutAdmins ->
        for (guild in guildsWithoutAdmins) {
            syncGuildAdmin(guild)
        }
        "Updated ${guildsWithoutAdmins.size} guilds."
    }

    private fun syncGuildAdmin(springGuild: SpringGuild): OperationResult<String, String> {
        when (val guild = discordService.getGuild(springGuild.guildId)) {
            is Failure -> {
                logger.error("Failed to retrieve guild ${springGuild.guildId} from JDA.")
                return Failure("Failed to retrieve guild ${springGuild.guildId} from JDA.")
            }

            is Success -> {
                val owner = guild.value.owner

                if (owner == null) {
                    logger.error("Failed to get guild owner for ${springGuild.guildId} from JDA.")
                    return Failure("Failed to get guild owner for ${springGuild.guildId} from JDA.")
                }

                springGuildService.addModerator(springGuild.guildId, owner.user.id)

                discordService.sendUserMessage(
                    "You have been added as a bot moderator for " +
                        "${discordService.getBotName()} in server ${guild.value.name} as you are the owner." +
                        " Use ${properties.privilegedCommandPrefix}help in your server for more information.",
                    owner.user.id,
                )
                logger.info("Set ${owner.user.id} as admin of ${springGuild.guildId}")
                return Success("Set ${owner.user.id} as admin of ${springGuild.guildId}")
            }
        }
    }

    fun getAdministrators(): OperationResult<List<Administrator>, String> = Success(repository.findAll())

    fun addRole(
        callerDiscordId: String,
        discordId: String,
        role: Role,
    ): OperationResult<Set<Role>, String> {
        when (val admin = getBotAdministratorByDiscordId(discordId)) {
            is Failure -> return admin

            is Success -> {
                if (hasRolesByDiscordId(callerDiscordId, Role.ManageAdmin) is Failure) {
                    return Failure("Does not have permission to manage administrators.")
                }

                if (admin.value.roles.contains(role)) return Success(admin.value.roles)

                val findAndModify =
                    mongoOperations.findAndModify(
                        Query(Criteria.where("_id").`is`(admin.value.id)),
                        Update().addToSet("roles", role),
                        Administrator::class.java,
                    )

                return Success(findAndModify?.roles ?: setOf())
            }
        }
    }

    fun removeRole(
        callerDiscordId: String,
        discordId: String,
        role: Role,
    ): OperationResult<Set<Role>, String> {
        when (val admin = getBotAdministratorByDiscordId(discordId)) {
            is Failure -> return admin

            is Success -> {
                if (hasRolesByDiscordId(callerDiscordId, Role.ManageAdmin) is Failure) {
                    return Failure("Does not have permission to manage administrators.")
                }

                if (!admin.value.roles.contains(role)) return Success(admin.value.roles)

                val findAndModify =
                    mongoOperations.findAndModify(
                        Query(Criteria.where("_id").`is`(admin.value.id)),
                        Update().pull("roles", role),
                        Administrator::class.java,
                    )
                return Success(findAndModify?.roles ?: setOf())
            }
        }
    }

    fun logToAdmins(message: String) {
        val admins =
            mongoOperations.find(
                Query(Criteria.where("roles").`in`(Role.Logging)),
                Administrator::class.java,
            )
        for (admin in admins) {
            if (admin.roles.contains(Role.Logging)) {
                logger.info("[System]: $message ${admin.discordId}")
                discordService.sendUserMessage("[System]: $message", admin.discordId)
            }
        }
    }

    fun getRoles(
        userId: String,
        discordId: String,
    ): OperationResult<Set<Role>, String> {
        when (val admin = getBotAdministratorByDiscordId(userId)) {
            is Failure -> return admin

            is Success -> {
                val id = admin.value.id
                if (hasRoles(id, Role.InspectAdmin) is Failure) {
                    return Failure("Does not have permission to view roles")
                }

                return getBotAdministratorByDiscordId(discordId).map { it.roles }
            }
        }
    }

    private fun hasRolesByDiscordId(
        discordId: String,
        vararg roles: Role,
    ): OperationResult<String, String> = getBotAdministratorByDiscordId(discordId).flatMap { hasRoles(it.id, *roles) }

    fun hasRoles(
        userId: String,
        vararg roles: Role,
    ): OperationResult<String, String> = getBotAdministratorById(userId).flatMap { admin ->
        if (!admin.roles.containsAll(roles.toList()) && !admin.roles.contains(Role.Primary)) {
            Failure("Does not have roles: $roles")
        } else {
            Success("Has roles: $roles")
        }
    }
}
