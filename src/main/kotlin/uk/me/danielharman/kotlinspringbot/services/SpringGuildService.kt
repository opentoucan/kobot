package uk.me.danielharman.kotlinspringbot.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.SpringGuild
import uk.me.danielharman.kotlinspringbot.repositories.GuildRepository
import java.util.stream.Collectors
import kotlin.math.max

@Service
class SpringGuildService(private val guildRepository: GuildRepository) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun getGuild(guildId: String): OperationResult<SpringGuild, String> {
        val guild = guildRepository.findByGuildId(guildId) ?: return Failure("Could not find guild $guildId")
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

    fun deleteSpringGuild(guildId: String): OperationResult<String, String> {
        return when (val guild = getGuild(guildId)) {
            is Failure -> guild
            is Success -> {
                guildRepository.deleteByGuildId(guild.value.guildId)
                Success(guild.value.id)
            }
        }
    }

    fun getGuilds(pageSize: Int = 10, page: Int = 0): OperationResult<List<SpringGuild>, String> =
        Success(guildRepository.findAll(PageRequest.of(max(page, 0), max(pageSize, 1))).toList())


    fun updateUserCount(guildId: String, userId: String, count: Int): OperationResult<SpringGuild, String> {
        val guild = createGuildIfNotExists(guildId)
        if (guild is Failure) return guild

        val result = guildRepository.increaseUserCount(guildId, userId, count) ?: return Failure("")

        return Success(result)
    }

    fun setVol(guildId: String, vol: Int): OperationResult<Int, String> {
        val guild = createGuildIfNotExists(guildId)
        if (guild is Failure) return guild

        val newVol = when {
            vol > 100 -> 100
            vol < 0 -> 0
            else -> vol
        }

        val findAndModify = guildRepository.setGuildVolume(guildId, newVol) ?: return Failure("Failed to update guild")

        return Success(findAndModify.volume)
    }

    fun getVol(guildId: String): OperationResult<Int, String> {
        return when (val guild = getGuild(guildId)) {
            is Failure -> guild
            is Success -> Success(guild.value.volume)
        }
    }

    fun isModerator(guildId: String, userId: String): OperationResult<String, String> {
        return when (val guild = getGuild(guildId)) {
            is Failure -> guild
            is Success -> {
                if (guild.value.privilegedUsers.contains(userId)) {
                    Success(userId)
                } else {
                    Failure("User is not a moderator")
                }
            }
        }
    }

    fun addModerator(guildId: String, userId: String): OperationResult<String, String> {
        return when (val guild = createGuildIfNotExists(guildId)) {
            is Failure -> guild
            is Success -> {
                guildRepository.addModeratorId(guild.value.guildId, userId)
                    ?: return Failure("Could not update guild")
                Success(userId)
            }
        }
    }

    fun removeModerator(guildId: String, userId: String): OperationResult<String, String> {
        when (val guild = getGuild(guildId)) {
            is Failure -> return guild
            is Success -> {
                guildRepository.removeModeratorId(guild.value.guildId, userId)
                    ?: return Failure("Could not update guild")
                return Success(userId)
            }
        }
    }

    fun addMemeChannel(guildId: String, channelId: String): OperationResult<String, String> {
        return when (val guild = createGuildIfNotExists(guildId)) {
            is Failure -> guild
            is Success -> {
                guildRepository.addMemeChannelId(guild.value.guildId, channelId)
                    ?: return Failure("Could not update guild")
                Success(channelId)
            }
        }
    }

    fun removeMemeChannel(guildId: String, channelId: String): OperationResult<String, String> {
        return when (val guild = getGuild(guildId)) {
            is Failure -> guild
            is Success -> {
                guildRepository.removeMemeChannelId(guild.value.guildId, channelId)
                    ?: return Failure("Could not update guild")
                Success(channelId)
            }
        }
    }

    fun getMemeChannels(guildId: String): OperationResult<List<String>, String> {
        return when (val guild = getGuild(guildId)) {
            is Failure -> guild
            is Success -> Success(guild.value.memeChannels)
        }
    }

    fun setXkcdChannel(guildId: String, channelId: String): OperationResult<String, String> {
        return when (val guild = createGuildIfNotExists(guildId)) {
            is Failure -> guild
            is Success -> {
                guildRepository.setXkcdChannelId(guild.value.guildId, channelId)
                    ?: return Failure("Could not update guild")
                Success(channelId)
            }
        }
    }

    fun getXkcdChannel(guildId: String): OperationResult<String, String> {
        return when (val guild = getGuild(guildId)) {
            is Failure -> guild
            is Success -> Success(guild.value.xkcdChannelId)
        }
    }

    fun getXkcdChannels(): OperationResult<List<String>, String> {
        val xkcdChannelIds = guildRepository.getXkcdChannelIds()
        return Success(xkcdChannelIds.stream().map { g -> g.xkcdChannelId }.filter { s -> s.isNotEmpty() }
            .collect(Collectors.toList()))
    }

    fun deafenChannel(guildId: String, channelId: String): OperationResult<String, String> {
        return when (val guild = getGuild(guildId)) {
            is Failure -> guild
            is Success -> {
                guildRepository.addDeafenChannelId(guild.value.guildId, channelId)
                    ?: return Failure("Could not update guild")
                Success(channelId)
            }
        }
    }

    fun unDeafenChannel(guildId: String, channelId: String): OperationResult<String, String> {
        return when (val guild = getGuild(guildId)) {
            is Failure -> guild
            is Success -> {
                guildRepository.removeDeafenChannelId(guild.value.guildId, channelId)
                    ?: return Failure("Could not update guild")
                Success(channelId)
            }
        }
    }

    fun getDeafenedChannels(guildId: String): OperationResult<List<String>, String> {
        return when (val guild = getGuild(guildId)) {
            is Failure -> guild
            is Success -> Success(guild.value.deafenedChannels)
        }
    }

    fun getGuildsWithoutModerators(): OperationResult<List<SpringGuild>, String> =
        Success(guildRepository.getGuildsWithModeratorCount(0))
}