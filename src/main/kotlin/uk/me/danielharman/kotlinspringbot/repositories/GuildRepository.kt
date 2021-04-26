package uk.me.danielharman.kotlinspringbot.repositories

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import uk.me.danielharman.kotlinspringbot.models.SpringGuild

@Repository
interface GuildRepository : MongoRepository<SpringGuild, String> {

    fun findByGuildId(guildId: String) : SpringGuild?
    fun deleteByGuildId(guildId: String)

}