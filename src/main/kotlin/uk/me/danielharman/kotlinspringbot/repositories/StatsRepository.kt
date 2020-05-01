package uk.me.danielharman.kotlinspringbot.repositories

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import uk.me.danielharman.kotlinspringbot.models.ServerStats

@Repository
interface StatsRepository : MongoRepository<ServerStats, String> {

    fun findByServerId(serverId: String) : ServerStats?

}