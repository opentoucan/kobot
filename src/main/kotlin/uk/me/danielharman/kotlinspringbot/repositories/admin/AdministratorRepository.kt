package uk.me.danielharman.kotlinspringbot.repositories.admin

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import uk.me.danielharman.kotlinspringbot.models.admin.Administrator

@Repository
interface AdministratorRepository : MongoRepository<Administrator, String> {
    fun getByDiscordId(discordId: String): Administrator?

    fun deleteByDiscordId(discordId: String)
}
