package uk.me.danielharman.kotlinspringbot.repositories

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.Update.update
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import uk.me.danielharman.kotlinspringbot.models.SpringGuild

@Repository
interface GuildRepository :
    MongoRepository<SpringGuild, String>,
    CustomGuildRepository {
    fun findByGuildId(guildId: String): SpringGuild?

    fun deleteByGuildId(guildId: String)
}

interface CustomGuildRepository {
    fun addToSet(
        guildId: String,
        field: String,
        value: String,
    ): SpringGuild?

    fun removeFromSet(
        guildId: String,
        field: String,
        value: String,
    ): SpringGuild?

    fun increaseUserCount(
        guildId: String,
        userId: String,
        value: Int,
    ): SpringGuild?

    fun setGuildVolume(
        guildId: String,
        volume: Int,
    ): SpringGuild?

    fun addModeratorId(
        guildId: String,
        userId: String,
    ): SpringGuild?

    fun removeModeratorId(
        guildId: String,
        userId: String,
    ): SpringGuild?

    fun addMemeChannelId(
        guildId: String,
        channelId: String,
    ): SpringGuild?

    fun removeMemeChannelId(
        guildId: String,
        channelId: String,
    ): SpringGuild?

    fun setXkcdChannelId(
        guildId: String,
        channelId: String,
    ): SpringGuild?

    fun getXkcdChannelIds(): List<SpringGuild>

    fun addDeafenChannelId(
        guildId: String,
        channelId: String,
    ): SpringGuild?

    fun removeDeafenChannelId(
        guildId: String,
        channelId: String,
    ): SpringGuild?

    fun getGuildsWithModeratorCount(value: Int): List<SpringGuild>
}

@Repository
class CustomGuildRepositoryImpl(
    private val mongoTemplate: MongoTemplate,
) : CustomGuildRepository {
    override fun addToSet(
        guildId: String,
        field: String,
        value: String,
    ): SpringGuild? = mongoTemplate.findAndModify(
        query(where(SpringGuild::guildId.name).`is`(guildId)),
        Update().addToSet(field, value),
        SpringGuild::class.java,
    )

    override fun removeFromSet(
        guildId: String,
        field: String,
        value: String,
    ): SpringGuild? = mongoTemplate.findAndModify(
        query(where(SpringGuild::guildId.name).`is`(guildId)),
        Update().pull(field, value),
        SpringGuild::class.java,
    )

    override fun increaseUserCount(
        guildId: String,
        userId: String,
        value: Int,
    ): SpringGuild? = mongoTemplate.findAndModify(
        query(where(SpringGuild::guildId.name).`is`(guildId)),
        Update().inc("${SpringGuild::userWordCounts.name}.$userId", value),
        SpringGuild::class.java,
    )

    override fun setGuildVolume(
        guildId: String,
        volume: Int,
    ): SpringGuild? = mongoTemplate.findAndModify(
        query(where(SpringGuild::guildId.name).`is`(guildId)),
        update(SpringGuild::volume.name, volume),
        SpringGuild::class.java,
    )

    override fun addModeratorId(
        guildId: String,
        userId: String,
    ): SpringGuild? = this.addToSet(guildId, SpringGuild::privilegedUsers.name, userId)

    override fun removeModeratorId(
        guildId: String,
        userId: String,
    ): SpringGuild? = this.removeFromSet(guildId, SpringGuild::privilegedUsers.name, userId)

    override fun addMemeChannelId(
        guildId: String,
        channelId: String,
    ): SpringGuild? = this.addToSet(guildId, SpringGuild::memeChannels.name, channelId)

    override fun removeMemeChannelId(
        guildId: String,
        channelId: String,
    ): SpringGuild? = this.removeFromSet(guildId, SpringGuild::memeChannels.name, channelId)

    override fun setXkcdChannelId(
        guildId: String,
        channelId: String,
    ): SpringGuild? = mongoTemplate.findAndModify(
        query(where(SpringGuild::guildId.name).`is`(guildId)),
        Update().set(SpringGuild::xkcdChannelId.name, channelId),
        SpringGuild::class.java,
    )

    override fun getXkcdChannelIds(): List<SpringGuild> {
        val query = Query()
        query.fields().include(SpringGuild::xkcdChannelId.name).include(SpringGuild::guildId.name)
        return mongoTemplate.find(query, SpringGuild::class.java)
    }

    override fun addDeafenChannelId(
        guildId: String,
        channelId: String,
    ): SpringGuild? = this.addToSet(guildId, SpringGuild::deafenedChannels.name, channelId)

    override fun removeDeafenChannelId(
        guildId: String,
        channelId: String,
    ): SpringGuild? = this.removeFromSet(guildId, SpringGuild::deafenedChannels.name, channelId)

    override fun getGuildsWithModeratorCount(value: Int): List<SpringGuild> = mongoTemplate.find(
        Query(where(SpringGuild::privilegedUsers.name).size(value)),
        SpringGuild::class.java,
    )
}
