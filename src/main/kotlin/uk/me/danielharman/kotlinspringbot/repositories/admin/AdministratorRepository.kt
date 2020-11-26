package uk.me.danielharman.kotlinspringbot.repositories.admin

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import uk.me.danielharman.kotlinspringbot.models.admin.Administrator
import uk.me.danielharman.kotlinspringbot.models.admin.enums.Role

@Repository
interface AdministratorRepository : MongoRepository<Administrator, String> {

    fun getByDiscordId(discordId: String): Administrator?
    //fun getAllByRole(role: Role): List<Administrator>
    fun deleteByDiscordId(discordId: String)

}