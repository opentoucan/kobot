package uk.me.danielharman.kotlinspringbot.services

import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.helpers.OperationHelpers.OperationResult
import uk.me.danielharman.kotlinspringbot.models.SpringGuild
import uk.me.danielharman.kotlinspringbot.repositories.GuildRepository
import java.util.stream.Collectors
import kotlin.math.max

@Service
class GuildService(private val guildRepository: GuildRepository, private val mongoTemplate: MongoTemplate) {

    fun getGuild(serverId: String): SpringGuild? = guildRepository.findByGuildId(serverId)
    fun createGuild(guildId: String): SpringGuild = guildRepository.save(SpringGuild(guildId))
    fun getGuilds(pageSize: Int = 10, page: Int = 0): List<SpringGuild> =
        guildRepository.findAll(PageRequest.of(max(page, 0),  max(pageSize, 1))).toList()


    fun updateUserCount(guildId: String, userId: String, count: Int) {
        val guild = getGuild(guildId)
        if (guild == null) {
            createGuild(guildId)
        }

        val update = Update()
        update.inc("userWordCounts.$userId", count)
        mongoTemplate.findAndModify(query(where("guildId").`is`(guildId)), update, SpringGuild::class.java)
    }

    fun setVol(guildId: String, vol: Int) {
        val guild = getGuild(guildId)
        if (guild == null) {
            createGuild(guildId)
        }

        val newVol = when {
            vol > 100 -> 100
            vol < 0 -> 0
            else -> vol
        }

        mongoTemplate.findAndModify(query(where("guildId").`is`(guildId)),
                Update.update("volume", newVol), SpringGuild::class.java)
    }

    fun getVol(guildId: String) = getGuild(guildId)?.volume ?: 50

    fun setGuildLogChannel(guildId: String, channelId: String) {
        val guild = getGuild(guildId)
        if (guild == null) {
            createGuild(guildId)
        }

        mongoTemplate.findAndModify(query(where("guildId").`is`(guildId)),
                Update().set("logChannelId", channelId), SpringGuild::class.java)
    }

    fun isPrivileged(guildId: String, userId: String): Boolean {
        val guild = getGuild(guildId) ?: return false
        return guild.privilegedUsers.contains(userId)
    }

    fun addPrivileged(guildId: String, userId: String): OperationResult<String?> {
        val guild = getGuild(guildId)
        if (guild == null) {
            createGuild(guildId)
        }

        mongoTemplate.findAndModify(query(where("guildId").`is`(guildId)),
                Update().addToSet("privilegedUsers", userId), SpringGuild::class.java)

        return OperationResult.successResult("Added $userId")
    }

    fun removedPrivileged(guildId: String, userId: String): OperationResult<String?> {

        val guild = getGuild(guildId) ?: return OperationResult.failResult("Could not find guild")

        val filter = guild.privilegedUsers.filter { s -> s != userId }

        mongoTemplate.findAndModify(query(where("guildId").`is`(guildId)),
                Update().set("privilegedUsers", filter), SpringGuild::class.java)
        return OperationResult.successResult("Removed $userId")
    }

    fun addMemeChannel(guildId: String, channelId: String) {
        mongoTemplate.findAndModify(query(where("guildId").`is`(guildId)),
                Update().addToSet("memeChannels", channelId), SpringGuild::class.java)
    }

    fun removeMemeChannel(guildId: String, channelId: String) {
        mongoTemplate.findAndModify(query(where("guildId").`is`(guildId)),
                Update().pull("memeChannels", channelId), SpringGuild::class.java)
    }

    fun getMemeChannels(guildId: String): List<String> = getGuild(guildId)?.memeChannels ?: listOf()

    fun setXkcdChannel(guildId: String, channelId: String) {
        val guild = getGuild(guildId)
        if (guild == null) {
            createGuild(guildId)
        }

        mongoTemplate.findAndModify(query(where("guildId").`is`(guildId)),
                Update().set("xkcdChannelId", channelId), SpringGuild::class.java)
    }

    fun getXkcdChannel(guildId: String): String = getGuild(guildId)?.xkcdChannelId ?: ""

    fun getXkcdChannels(): List<String>{

        val query = Query()
        query.fields().include("xkcdChannelId").include("guildId")

        val list = mongoTemplate.find(query, SpringGuild::class.java)
        return list.stream().map { g -> g.xkcdChannelId }.filter { s -> s.isNotEmpty() }.collect(Collectors.toList())
    }

    fun deafenChannel(guildId: String, channelId: String): Boolean {
        val guild = getGuild(guildId)
        if (guild == null) {
            createGuild(guildId)
        }

        mongoTemplate.findAndModify(query(where("guildId").`is`(guildId)),
                Update().addToSet("deafenedChannels", channelId), SpringGuild::class.java)
        return true
    }

    fun unDeafenChannel(guildId: String, channelId: String): Boolean {
        getGuild(guildId) ?: return false

        mongoTemplate.findAndModify(query(where("guildId").`is`(guildId)),
                Update().pull("deafenedChannels", channelId), SpringGuild::class.java)
        return true
    }

    fun getDeafenedChannels(guildId: String): List<String> = getGuild(guildId)?.deafenedChannels ?: listOf()

    fun getGuildsWithoutAdmins(): List<SpringGuild> {
        return mongoTemplate.find(Query(where("privilegedUsers").size(0)), SpringGuild::class.java);
    }

    fun deleteSpringGuild(guildId: String): OperationResult<String?> {
        guildRepository.deleteByGuildId(guildId)
        return OperationResult.successResult("Deleted")
    }

}