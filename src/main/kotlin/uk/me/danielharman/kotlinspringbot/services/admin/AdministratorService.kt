package uk.me.danielharman.kotlinspringbot.services.admin

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.helpers.OperationHelpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.OperationHelpers.OperationResult.Companion.failResult
import uk.me.danielharman.kotlinspringbot.helpers.OperationHelpers.OperationResult.Companion.successResult
import uk.me.danielharman.kotlinspringbot.models.SpringGuild
import uk.me.danielharman.kotlinspringbot.models.admin.Administrator
import uk.me.danielharman.kotlinspringbot.models.admin.enums.Role
import uk.me.danielharman.kotlinspringbot.repositories.admin.AdministratorRepository
import uk.me.danielharman.kotlinspringbot.services.DiscordService
import uk.me.danielharman.kotlinspringbot.services.GuildService

@Service
class AdministratorService (private val repository: AdministratorRepository,
                            private val props: KotlinBotProperties,
                            private val guildService: GuildService,
                            private val discordService: DiscordService,
                            private val properties: KotlinBotProperties,
                            private val mongoOperations: MongoOperations) {

    private val logger : Logger = LoggerFactory.getLogger(this::class.java)

    fun getAdminById(id: String): OperationResult<Administrator?>{
        val administrator = repository.findById(id)

        if(administrator.isEmpty)
            return failResult("Administrator not found")

        return successResult(administrator.get())
    }

    fun getAdminByDiscordId(id: String): OperationResult<Administrator?>{
        val administrator = repository.getByDiscordId(id) ?: return failResult("Administrator not found")
        return successResult(administrator)
    }

    fun createAdmin(id: String, roles: Set<Role>): OperationResult<Administrator?>{
        val administrator = repository.save(Administrator(id, roles))

        return successResult(administrator)
    }

    fun removeAdmin(id: String) : OperationResult<String?>{
        if (id == props.primaryPrivilegedUserId){
            return failResult("Cannot remove primary admin")
        }
        repository.deleteByDiscordId(id)
        return successResult("Deleted")
    }

    fun syncGuildAdmins() : OperationResult<String?>{
        val guildsWithoutAdmins = guildService.getGuildsWithoutAdmins()

        for(guild in guildsWithoutAdmins){
            syncGuildAdmin(guild)
        }
        return successResult("Updated ${guildsWithoutAdmins.size} guilds.")
    }

    private fun syncGuildAdmin(springGuild: SpringGuild){
        val guild = discordService.getGuild(springGuild.guildId)

        if(guild == null){
            logger.error("Failed to retrieve guild ${springGuild.guildId} from JDA.")
            return
        }

        val owner = guild.owner

        if(owner == null){
            logger.error("Failed to get guild owner for ${springGuild.guildId} from JDA.")
            return
        }

        guildService.addPrivileged(springGuild.guildId, owner.user.id)

        logger.info("Set ${owner.user.id} as admin of ${springGuild.guildId}")

        discordService.sendUserMessage("You have been added as a bot administrator for ${discordService.getBotName()} in server ${guild.name} as you are the owner." +
                " Use ${properties.privilegedCommandPrefix}help in your server for more information.", owner.user.id)
    }
}