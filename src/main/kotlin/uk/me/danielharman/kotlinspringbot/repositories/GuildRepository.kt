package uk.me.danielharman.kotlinspringbot.repositories

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import uk.me.danielharman.kotlinspringbot.models.Guild

@Repository
interface GuildRepository : MongoRepository<Guild, String> {

    fun findByGuildId(guildId: String) : Guild?

}