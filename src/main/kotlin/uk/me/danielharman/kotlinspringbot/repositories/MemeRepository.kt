package uk.me.danielharman.kotlinspringbot.repositories

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import uk.me.danielharman.kotlinspringbot.models.Meme

@Repository
interface MemeRepository : MongoRepository<Meme, String> {

    fun findByGuildId(guildId: String): Meme?
    fun findByUserId(userId: String): List<Meme>

}