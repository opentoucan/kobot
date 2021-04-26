package uk.me.danielharman.kotlinspringbot.services.admin

import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.helpers.OperationHelpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.OperationHelpers.OperationResult.Companion.failResult
import uk.me.danielharman.kotlinspringbot.helpers.OperationHelpers.OperationResult.Companion.successResult
import uk.me.danielharman.kotlinspringbot.models.SpringGuild
import uk.me.danielharman.kotlinspringbot.models.admin.Administrator
import uk.me.danielharman.kotlinspringbot.models.admin.enums.Role
import uk.me.danielharman.kotlinspringbot.objects.ApplicationInfo
import uk.me.danielharman.kotlinspringbot.repositories.admin.AdministratorRepository
import uk.me.danielharman.kotlinspringbot.services.DiscordService
import uk.me.danielharman.kotlinspringbot.services.GuildService

@Service
class AdministratorService(
    private val repository: AdministratorRepository,
    private val guildService: GuildService,
    private val discordService: DiscordService,
    private val properties: KotlinBotProperties,
    private val mongoOperations: MongoOperations
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun getBotVersion(): OperationResult<String?> = successResult(ApplicationInfo.version)

    fun getBotStartTime(): OperationResult<DateTime?> = successResult(ApplicationInfo.startTime)

    fun getDiscordStartTime(): OperationResult<DateTime?> = discordService.getDiscordStartTime()

    fun getBotDiscordName(): OperationResult<String?> = successResult(discordService.getBotName())

    fun closeDiscordConnection(userId: String): OperationResult<String?> {
        val admin = getBotAdministratorById(userId)
        if (admin.failure)
            return failResult(admin.message)
        return discordService.closeDiscordConnection()
    }

    fun startDiscordConnection(userId: String): OperationResult<String?> {
        val admin = getBotAdministratorById(userId)
        if (admin.failure)
            return failResult(admin.message)
        return discordService.startDiscordConnection()
    }

    fun restartDiscordConnection(userId: String): OperationResult<String?> {
        val closeDiscordConnection = closeDiscordConnection(userId)
        if (closeDiscordConnection.failure) {
            return closeDiscordConnection
        }
        return startDiscordConnection(userId)
    }

    fun getBotAdministratorById(id: String): OperationResult<Administrator?> {
        val administrator = repository.findById(id)

        if (administrator.isEmpty)
            return failResult("Administrator not found")

        return successResult(administrator.get())
    }

    fun getBotAdministratorByDiscordId(id: String): OperationResult<Administrator?> {
        val administrator = repository.getByDiscordId(id) ?: return failResult("Administrator not found")
        return successResult(administrator)
    }

    fun createBotAdministrator(discordId: String, roles: Set<Role> = setOf()): OperationResult<Administrator?> {
        val admin = getBotAdministratorByDiscordId(discordId)
        if (admin.success) return admin
        return successResult(repository.save(Administrator(discordId, roles)))
    }

    fun createBotAdministrator(userId: String, discordId: String, roles: Set<Role>): OperationResult<Administrator?> {
        val admin = getBotAdministratorById(userId)
        if (admin.failure)
            return admin

        val administrator = repository.save(Administrator(discordId, roles))

        logToAdmins("${administrator.discordId} added as admin by $userId")

        return successResult(administrator)
    }

    fun removeBotAdministrator(id: String): OperationResult<String?> {
        if (id == properties.primaryPrivilegedUserId) {
            return failResult("Cannot remove primary admin")
        }
        repository.deleteByDiscordId(id)
        return successResult("Deleted")
    }

    fun addSpringGuildAdministrator(userId: String, guildId: String, newAdminId: String): OperationResult<String?> {
        //TODO: Permissions
        val guild = guildService.getGuild(guildId) ?: return failResult("No such guild.")

        if (!guild.privilegedUsers.contains(userId)) {
            return failResult("Insufficient permissions.")
        }

        return guildService.addPrivileged(guildId, newAdminId)
    }

    fun removeSpringGuildAdministrator(userId: String, guildId: String, adminId: String): OperationResult<String?> {
        //TODO: Permissions
        val guild = guildService.getGuild(guildId) ?: return failResult("No such guild.")

        if (!guild.privilegedUsers.contains(userId)) {
            return failResult("Insufficient permissions.")
        }

        return guildService.removedPrivileged(guildId, adminId)
    }

    fun deleteSpringGuild(userId: String, guildId: String): OperationResult<String?> {
        //TODO: Permissions
        val guild = guildService.getGuild(guildId) ?: return failResult("No such guild.")

        if (!guild.privilegedUsers.contains(userId)) {
            return failResult("Insufficient permissions.")
        }

        return guildService.deleteSpringGuild(guildId)
    }

    fun getSpringGuild(userId: String, guildId: String): OperationResult<SpringGuild?> {
        //TODO: Permissions
        val guild = guildService.getGuild(guildId) ?: return failResult("No such guild")

        if (!guild.privilegedUsers.contains(userId)) {
            return failResult("Insufficient permissions.")
        }
        return successResult(guild)
    }

    fun syncGuildAdmins(): OperationResult<String?> {
        val guildsWithoutAdmins = guildService.getGuildsWithoutAdmins()

        for (guild in guildsWithoutAdmins) {
            syncGuildAdmin(guild)
        }
        return successResult("Updated ${guildsWithoutAdmins.size} guilds.")
    }

    private fun syncGuildAdmin(springGuild: SpringGuild) {
        val guild = discordService.getGuild(springGuild.guildId)

        if (guild == null) {
            logger.error("Failed to retrieve guild ${springGuild.guildId} from JDA.")
            return
        }

        val owner = guild.owner

        if (owner == null) {
            logger.error("Failed to get guild owner for ${springGuild.guildId} from JDA.")
            return
        }

        guildService.addPrivileged(springGuild.guildId, owner.user.id)

        logger.info("Set ${owner.user.id} as admin of ${springGuild.guildId}")

        discordService.sendUserMessage(
            "You have been added as a bot administrator for ${discordService.getBotName()} in server ${guild.name} as you are the owner." +
                    " Use ${properties.privilegedCommandPrefix}help in your server for more information.", owner.user.id
        )
    }

    fun getAdministrators(userId: String): OperationResult<List<Administrator>?> {
        //TODO Permissions
        return successResult(repository.findAll())
    }

    fun addRole(userId: String, discordId: String, role: Role): OperationResult<Set<Role>?> {
        val admin = getBotAdministratorByDiscordId(discordId)

        if (admin.failure)
            return failResult(admin.message)

        if(!hasRoles(userId, Role.ManageAdmin))
            return failResult("Does not have permission to manage administrators.")

        if (admin.value?.roles?.contains(role) == true)
            return successResult(admin.value.roles)

        val findAndModify = mongoOperations.findAndModify(
            Query(Criteria.where("_id").`is`(admin.value?.id)),
            Update().addToSet("roles", role),
            Administrator::class.java
        )

        return successResult(findAndModify?.roles ?: setOf())
    }

    fun removeRole(userId: String, discordId: String, role: Role): OperationResult<Set<Role>?> {
        val admin = getBotAdministratorByDiscordId(discordId)

        if (admin.failure)
            return failResult(admin.message)

        if(!hasRoles(userId, Role.ManageAdmin))
            return failResult("Does not have permission to manage administrators.")

        if (admin.value?.roles?.contains(role) == false)
            return successResult(admin.value.roles)

        val findAndModify = mongoOperations.findAndModify(
            Query(Criteria.where("_id").`is`(admin.value?.id)),
            Update().pull("roles", role),
            Administrator::class.java
        )

        return successResult(findAndModify?.roles ?: setOf())
    }

    fun logToAdmins(message: String) {
        val admins = mongoOperations.find(Query(Criteria.where("roles").`in`(Role.Logging)), Administrator::class.java)
        for (admin in admins) {
            if (admin.roles.contains(Role.Logging)) {
                logger.info("[System]: $message ${admin.discordId}")
                discordService.sendUserMessage("[System]: $message", admin.discordId)
            }
        }
    }

    fun getRoles(userId: String, discordId: String): OperationResult<Set<Role>?> {
        val admin = getBotAdministratorByDiscordId(userId)
        if (admin.failure)
            return failResult(admin.message)
        val id = admin.value?.id ?: ""
        if(!hasRoles(id, Role.InspectAdmin))
            return failResult("Does not have permission to view roles")

        val user = getBotAdministratorByDiscordId(discordId)
        if (user.failure)
            return failResult(user.message)

        return successResult(user.value?.roles)
    }

    fun hasRoles(userId: String, vararg roles: Role, ): Boolean{
        val admin = getBotAdministratorById(userId)
        if (admin.failure) return false
        val adminRoles = admin.value?.roles
        return (adminRoles?.containsAll(roles.toList()) == true || adminRoles?.contains(Role.Primary) == true)
    }

}