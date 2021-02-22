package uk.me.danielharman.kotlinspringbot.repositories

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import uk.me.danielharman.kotlinspringbot.models.DiscordCommand

@Repository
interface DiscordCommandRepository : MongoRepository<DiscordCommand, String> {

    fun findFirstByGuildIdAndKey(guildId:String, key: String): DiscordCommand?
    fun findAllByGuildId(guildId: String, pageable: Pageable): Page<DiscordCommand>
    fun countByGuildId(guildId: String): Long

}