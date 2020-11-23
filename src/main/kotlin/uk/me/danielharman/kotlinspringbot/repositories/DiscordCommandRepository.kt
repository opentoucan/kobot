package uk.me.danielharman.kotlinspringbot.repositories

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import uk.me.danielharman.kotlinspringbot.models.DiscordCommand

@Repository
interface DiscordCommandRepository : MongoRepository<DiscordCommand, String> {

    fun findByGuildIdAndKey(guildId:String, key: String): DiscordCommand?
    fun findAllByGuildId(guildId: String): List<DiscordCommand>

}