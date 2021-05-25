package uk.me.danielharman.kotlinspringbot.services.audit

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.audit.AuditItem
import uk.me.danielharman.kotlinspringbot.repositories.AuditRepository
import java.lang.Integer.max

@Service
class AuditService(private val auditRepository: AuditRepository) {

    fun audit(discordId: String, administratorId: String, action: String): OperationResult<AuditItem, String> =
        Success(auditRepository.save(AuditItem(discordId, administratorId, action)))

    fun getAudit(id: String): OperationResult<AuditItem, String> {
        val findById = auditRepository.findById(id)
        return if (findById.isEmpty)
            Failure("Audit not found")
        else
            Success(findById.get())
    }

    fun getAudits(page: Int = 0, pageSize: Int = 25): OperationResult<List<AuditItem>, String>
        = Success(auditRepository.findAll(PageRequest.of(max(0, page), max(0, pageSize))).toList())


    fun getAuditsByDiscordId(discordId: String, page: Int = 0, pageSize: Int = 25): OperationResult<List<AuditItem>, String>
        = Success(auditRepository.getAllByDiscordId(discordId,
                PageRequest.of(max(0, page), max(0, pageSize), Sort.by(Sort.Order.desc("id")))).toList())


    fun getAuditsByAdministratorId(administratorId: String, page: Int = 0, pageSize: Int = 25): OperationResult<List<AuditItem>, String>
        = Success(auditRepository.getAllByAdministratorId(administratorId,
                PageRequest.of(max(0, page), max(0, pageSize), Sort.by(Sort.Order.desc("id")))).toList())

}