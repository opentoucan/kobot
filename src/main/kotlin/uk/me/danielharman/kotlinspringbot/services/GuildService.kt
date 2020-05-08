package uk.me.danielharman.kotlinspringbot.services

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.models.SpringGuild
import uk.me.danielharman.kotlinspringbot.repositories.GuildRepository

@Service
class GuildService(private val guildRepository: GuildRepository, private val mongoTemplate: MongoTemplate) {

    fun getGuild(serverId: String): SpringGuild? = guildRepository.findByGuildId(serverId)
    fun createGuild(guildId: String): SpringGuild = guildRepository.save(SpringGuild(guildId))

    fun updateUserCount(guildId: String, userId: String, count: Int) {
        val update = Update()
        update.inc("userWordCounts.$userId", count)
        mongoTemplate.findAndModify(query(where("guildId").`is`(guildId)), update, SpringGuild::class.java)
    }

    fun setVol(guildId: String, vol: Int) {

        getGuild(guildId) ?: return

        val newVol = when {
            vol > 100 -> 100
            vol < 0 -> 0
            else -> vol
        }

        mongoTemplate.findAndModify(query(where("guildId").`is`(guildId)),
                Update.update("volume", newVol), SpringGuild::class.java )
    }

    fun getVol(guildId: String) = getGuild(guildId)?.volume ?: 50

    fun saveCommand(guildId: String, command: String, phrase: String) {
        val guild = getGuild(guildId)
        if (guild == null) {
            createGuild(guildId)
        }
        val update = Update()
        update.set("savedCommands.$command", phrase)
        mongoTemplate.findAndModify(query(where("guildId").`is`(guildId)), update, SpringGuild::class.java)
    }

    fun getCommand(guildId: String, command: String): String {
        val guild = getGuild(guildId)

        return if (guild != null) {
            guild.savedCommands[command] ?: "No such command"
        } else
            "Guild was not found"
    }

    fun setGuildLogChannel(guildId: String, channelId: String) {
        mongoTemplate.findAndModify(query(where("guildId").`is`(guildId)),
                Update().set("logChannelId", channelId), SpringGuild::class.java)
    }

    fun isPrivileged(guildId: String, userId: String): Boolean {
        val guild = getGuild(guildId) ?: return false
        return guild.privilegedUsers.contains(userId)
    }

    fun addPrivileged(guildId: String, userId: String) {
        mongoTemplate.findAndModify(query(where("guildId").`is`(guildId)),
                Update().addToSet("privilegedUsers", userId), SpringGuild::class.java)
    }

    fun removedPrivileged(guildId: String, userId: String) {

        val guild = getGuild(guildId) ?: return

        val filter = guild.privilegedUsers.filter { s -> s != userId }

        mongoTemplate.findAndModify(query(where("guildId").`is`(guildId)),
                Update().set("privilegedUsers", filter), SpringGuild::class.java)
    }

}