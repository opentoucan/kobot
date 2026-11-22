package uk.me.danielharman.kotlinspringbot.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.helpers.flatMap
import uk.me.danielharman.kotlinspringbot.helpers.map
import uk.me.danielharman.kotlinspringbot.models.SpringGuild
import uk.me.danielharman.kotlinspringbot.repositories.GuildRepository
import kotlin.math.max

private const val MAX_VOLUME = 100

@Service
class SpringGuildService(
    private val guildRepository: GuildRepository,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun getGuild(guildId: String): OperationResult<SpringGuild, String> {
        val guild =
            guildRepository.findByGuildId(guildId)
                ?: return Failure("Could not find guild $guildId")
        return Success(guild)
    }

    fun createGuild(guildId: String): OperationResult<SpringGuild, String> {
        logger.info("Creating guild $guildId")
        return when (getGuild(guildId)) {
            is Failure -> Success(guildRepository.save(SpringGuild(guildId)))
            is Success -> Failure("Guild already exists")
        }
    }

    fun createGuildIfNotExists(guildId: String): OperationResult<SpringGuild, String> {
        val guild = getGuild(guildId)
        if (guild is Success) return guild
        return Success(guildRepository.save(SpringGuild(guildId)))
    }

    fun deleteSpringGuild(guildId: String): OperationResult<String, String> = getGuild(guildId).map { guild ->
        guildRepository.deleteByGuildId(guild.guildId)
        guild.id
    }

    fun getGuilds(
        pageSize: Int = 10,
        page: Int = 0,
    ): OperationResult<List<SpringGuild>, String> = Success(guildRepository.findAll(PageRequest.of(max(page, 0), max(pageSize, 1))).toList())

    fun updateUserCount(
        guildId: String,
        userId: String,
        count: Int,
    ): OperationResult<SpringGuild, String> {
        val guild = createGuildIfNotExists(guildId)
        if (guild is Failure) return guild

        val result = guildRepository.increaseUserCount(guildId, userId, count) ?: return Failure("")

        return Success(result)
    }

    fun setVol(
        guildId: String,
        vol: Int,
    ): OperationResult<Int, String> {
        val guild = createGuildIfNotExists(guildId)
        if (guild is Failure) return guild

        val newVol =
            when {
                vol > MAX_VOLUME -> MAX_VOLUME
                vol < 0 -> 0
                else -> vol
            }

        val findAndModify =
            guildRepository.setGuildVolume(guildId, newVol)
                ?: return Failure("Failed to update guild")

        return Success(findAndModify.volume)
    }

    fun getVol(guildId: String): OperationResult<Int, String> = getGuild(guildId).map { it.volume }

    fun isModerator(
        guildId: String,
        userId: String,
    ): OperationResult<String, String> = getGuild(guildId).flatMap { guild ->
        if (guild.privilegedUsers.contains(userId)) {
            Success(userId)
        } else {
            Failure("User is not a moderator")
        }
    }

    fun addModerator(
        guildId: String,
        userId: String,
    ): OperationResult<String, String> = createGuildIfNotExists(guildId).flatMap { guild ->
        guildRepository.addModeratorId(guild.guildId, userId)
            ?.let { Success(userId) }
            ?: Failure("Could not update guild")
    }

    fun removeModerator(
        guildId: String,
        userId: String,
    ): OperationResult<String, String> = getGuild(guildId).flatMap { guild ->
        guildRepository.removeModeratorId(guild.guildId, userId)
            ?.let { Success(userId) }
            ?: Failure("Could not update guild")
    }

    fun addMemeChannel(
        guildId: String,
        channelId: String,
    ): OperationResult<String, String> = createGuildIfNotExists(guildId).flatMap { guild ->
        guildRepository.addMemeChannelId(guild.guildId, channelId)
            ?.let { Success(channelId) }
            ?: Failure("Could not update guild")
    }

    fun removeMemeChannel(
        guildId: String,
        channelId: String,
    ): OperationResult<String, String> = getGuild(guildId).flatMap { guild ->
        guildRepository.removeMemeChannelId(guild.guildId, channelId)
            ?.let { Success(channelId) }
            ?: Failure("Could not update guild")
    }

    fun getMemeChannels(guildId: String): OperationResult<List<String>, String> = getGuild(guildId).map { it.memeChannels }

    fun deafenChannel(
        guildId: String,
        channelId: String,
    ): OperationResult<String, String> = getGuild(guildId).flatMap { guild ->
        guildRepository.addDeafenChannelId(guild.guildId, channelId)
            ?.let { Success(channelId) }
            ?: Failure("Could not update guild")
    }

    fun unDeafenChannel(
        guildId: String,
        channelId: String,
    ): OperationResult<String, String> = getGuild(guildId).flatMap { guild ->
        guildRepository.removeDeafenChannelId(guild.guildId, channelId)
            ?.let { Success(channelId) }
            ?: Failure("Could not update guild")
    }

    fun isChannelDeafened(
        guildId: String,
        channelId: String,
    ): Boolean = when (val guild = getGuild(guildId)) {
        is Failure -> false
        is Success -> guild.value.deafenedChannels.contains(channelId)
    }

    fun getDeafenedChannels(guildId: String): OperationResult<List<String>, String> = getGuild(guildId).map { it.deafenedChannels }

    fun getGuildsWithoutModerators(): OperationResult<List<SpringGuild>, String> = Success(
        guildRepository.getGuildsWithModeratorCount(0),
    )
}
