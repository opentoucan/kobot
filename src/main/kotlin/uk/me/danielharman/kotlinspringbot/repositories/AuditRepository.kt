package uk.me.danielharman.kotlinspringbot.repositories

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import uk.me.danielharman.kotlinspringbot.models.audit.AuditItem

@Repository
interface AuditRepository: MongoRepository<AuditItem, String> {

    fun getAllByDiscordId(discordId: String, pageable: Pageable): Page<AuditItem>
    fun getAllByAdministratorId(administratorId: String, pageable: Pageable): Page<AuditItem>

}