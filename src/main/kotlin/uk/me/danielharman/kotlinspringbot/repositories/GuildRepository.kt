package uk.me.danielharman.kotlinspringbot.repositories

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.Update.update
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import uk.me.danielharman.kotlinspringbot.models.SpringGuild

@Repository
interface GuildRepository : MongoRepository<SpringGuild, String>, CustomGuildRepository {

    fun findByGuildId(guildId: String): SpringGuild?
    fun deleteByGuildId(guildId: String)

}

interface CustomGuildRepository {
    fun increaseUserCount(guildId: String, userId: String, value: Int): SpringGuild?
    fun setGuildVolume(guildId: String, volume: Int): SpringGuild?
    fun addModeratorId(guildId: String, userId: String): SpringGuild?
    fun removeModeratorId(guildId: String, userId: String): SpringGuild?
}

@Repository
class CustomGuildRepositoryImpl(private val mongoTemplate: MongoTemplate) : CustomGuildRepository {

    override fun increaseUserCount(guildId: String, userId: String, value: Int): SpringGuild? {
        return mongoTemplate.findAndModify(
            query(where(SpringGuild::guildId.name).`is`(guildId)),
            Update().inc("${SpringGuild::userWordCounts.name}.$userId", value),
            SpringGuild::class.java
        )
    }

    override fun setGuildVolume(guildId: String, volume: Int): SpringGuild? {
        return mongoTemplate.findAndModify(
            query(where(SpringGuild::guildId.name).`is`(guildId)),
            update(SpringGuild::volume.name, volume),
            SpringGuild::class.java
        )
    }

    override fun addModeratorId(guildId: String, userId: String): SpringGuild? {
        return mongoTemplate.findAndModify(
            query(where(SpringGuild::guildId.name).`is`(guildId)),
            Update().addToSet(SpringGuild::privilegedUsers.name, userId),
            SpringGuild::class.java
        )
    }

    override fun removeModeratorId(guildId: String, userId: String): SpringGuild? {
        return mongoTemplate.findAndModify(
            query(where(SpringGuild::guildId.name).`is`(guildId)),
            Update().pull(SpringGuild::privilegedUsers.name, userId),
            SpringGuild::class.java
        )
    }

}
