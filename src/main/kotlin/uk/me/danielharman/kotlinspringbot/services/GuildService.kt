package uk.me.danielharman.kotlinspringbot.services

import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.SpringGuild
import uk.me.danielharman.kotlinspringbot.repositories.GuildRepository
import java.util.stream.Collectors
import kotlin.math.max

@Service
class GuildService(private val guildRepository: GuildRepository, private val mongoTemplate: MongoTemplate) {

    //TODO: Look at utilising the custom queries on Spring repositories over mongoTemplate
    private val DATA_CLASS = SpringGuild::class.java

    fun getGuild(guildId: String): OperationResult<SpringGuild, String> {
        val guild = guildRepository.findByGuildId(guildId) ?: return Failure("Could not find guild $guildId")
        return Success(guild)
    }

    fun createGuild(guildId: String): OperationResult<SpringGuild, String> {
        if (getGuild(guildId) is Success) return Failure("Guild already exists")
        return Success(guildRepository.save(SpringGuild(guildId)))
    }

    fun createGuildIfNotExists(guildId: String): OperationResult<SpringGuild, String> {
        val guild = getGuild(guildId)
        if (guild is Success) return guild
        return Success(guildRepository.save(SpringGuild(guildId)))
    }

    fun getGuilds(pageSize: Int = 10, page: Int = 0): OperationResult<List<SpringGuild>, String> =
        Success(guildRepository.findAll(PageRequest.of(max(page, 0), max(pageSize, 1))).toList())


    fun updateUserCount(guildId: String, userId: String, count: Int): OperationResult<SpringGuild, String> {
        val guild = createGuildIfNotExists(guildId)
        if (guild is Failure) return guild

        val result =
            mongoTemplate.findAndModify(
                query(where("_id").`is`((guild as Success).value.id)),
                Update().inc("userWordCounts.$userId", count),
                DATA_CLASS
            ) ?: return Failure("No guild was found")
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

        val findAndModify = mongoTemplate.findAndModify(
            query(where("_id").`is`((guild as Success).value.id)),
            Update.update("volume", newVol), DATA_CLASS
        ) ?: return Failure("Failed to update guild")

        return Success(findAndModify.volume)
    }

    fun getVol(guildId: String): OperationResult<Int, String> {
        val guild = getGuild(guildId)
        if (guild is Failure) return Failure(guild.reason)
        return Success((guild as Success).value.volume)
    }

    fun isModerator(guildId: String, userId: String): OperationResult<String, String> {
        val guild = getGuild(guildId)
        if (guild is Failure) return guild

        return if ((guild as Success).value.privilegedUsers.contains(userId)) {
            Success(userId)
        } else {
            Failure("User is not a moderator")
        }
    }

    fun addModerator(guildId: String, userId: String): OperationResult<String, String> {
        val guild = createGuildIfNotExists(guildId)
        if (guild is Failure) return guild

        mongoTemplate.findAndModify(
            query(where("guildId").`is`(guildId)),
            Update().addToSet("privilegedUsers", userId), DATA_CLASS
        ) ?: return Failure("Could not update guild")

        return Success(userId)
    }

    fun removeModerator(guildId: String, userId: String): OperationResult<String, String> {

        val getGuild = getGuild(guildId)
        if (getGuild is Failure) return getGuild
        val guild = (getGuild as Success).value

        mongoTemplate.findAndModify(
            query(where("_id").`is`(guild.id)),
            Update().set("privilegedUsers", guild.privilegedUsers.filter { s -> s != userId }),
            DATA_CLASS
        ) ?: return Failure("Could not update guild")

        return Success(userId)
    }

    fun addMemeChannel(guildId: String, channelId: String): OperationResult<String, String> {
        val guild = (createGuildIfNotExists(guildId) as Success).value

        mongoTemplate.findAndModify(
            query(where("_id").`is`(guild.id)),
            Update().addToSet("memeChannels", channelId),
            DATA_CLASS
        ) ?: return Failure("Could not update guild")

        return Success(channelId)
    }

    fun removeMemeChannel(guildId: String, channelId: String): OperationResult<String, String> {
        val getGuild = getGuild(guildId)
        if (getGuild is Failure) return getGuild
        val guild = (getGuild as Success).value

        mongoTemplate.findAndModify(
            query(where("_id").`is`(guild.id)),
            Update().pull("memeChannels", channelId), DATA_CLASS
        ) ?: return Failure("Could not update guild")

        return Success(channelId)
    }

    fun getMemeChannels(guildId: String): OperationResult<List<String>, String> {
        val getGuild = getGuild(guildId)
        if (getGuild is Failure) return getGuild
        val guild = (getGuild as Success).value
        return Success(guild.memeChannels)
    }

    fun setXkcdChannel(guildId: String, channelId: String): OperationResult<String, String> {
        val guild = (createGuildIfNotExists(guildId) as Success).value

        mongoTemplate.findAndModify(
            query(where("_id").`is`(guild.id)),
            Update().set("xkcdChannelId", channelId),
            DATA_CLASS
        ) ?: return Failure("Could not update guild")

        return Success(channelId)
    }

    fun getXkcdChannel(guildId: String): OperationResult<String, String> {
        val getGuild = getGuild(guildId)
        if (getGuild is Failure) return getGuild
        val guild = (getGuild as Success).value
        return Success(guild.xkcdChannelId)
    }

    fun getXkcdChannels(): OperationResult<List<String>, String> {
        val query = Query()
        query.fields().include("xkcdChannelId").include("guildId")
        val list = mongoTemplate.find(query, DATA_CLASS)
        return Success(list.stream().map { g -> g.xkcdChannelId }.filter { s -> s.isNotEmpty() }
            .collect(Collectors.toList()))
    }

    fun deafenChannel(guildId: String, channelId: String, deafen: Boolean = true): OperationResult<String, String> {
        val getGuild = getGuild(guildId)
        if (getGuild is Failure) return getGuild
        val guild = (getGuild as Success).value

        val update = Update()
        if (deafen) {
            update.addToSet("deafenedChannels", channelId)
        } else {
            update.pull("deafenedChannels", channelId)
        }
        mongoTemplate.findAndModify(
            query(where("_id").`is`(guild.id)), update, DATA_CLASS
        )
        return Success(channelId)
    }

    fun unDeafenChannel(guildId: String, channelId: String): OperationResult<String, String> =
        deafenChannel(guildId, channelId, false)

    fun getDeafenedChannels(guildId: String): OperationResult<List<String>, String> {
        val getGuild = getGuild(guildId)
        if (getGuild is Failure) return getGuild
        val guild = (getGuild as Success).value
        return Success(guild.deafenedChannels)
    }

    fun getGuildsWithoutModerators(): OperationResult<List<SpringGuild>, String> =
        Success(mongoTemplate.find(Query(where("privilegedUsers").size(0)), DATA_CLASS))


    fun deleteSpringGuild(guildId: String): OperationResult<String, String> {
        val getGuild = getGuild(guildId)
        if (getGuild is Failure) return getGuild
        val guild = (getGuild as Success).value
        guildRepository.deleteByGuildId(guild.guildId)
        return Success(guild.id)
    }
}