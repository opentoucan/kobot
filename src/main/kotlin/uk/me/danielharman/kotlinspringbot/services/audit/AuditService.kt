package uk.me.danielharman.kotlinspringbot.services.audit

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.helpers.OperationHelpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.OperationHelpers.OperationResult.Companion.failResult
import uk.me.danielharman.kotlinspringbot.helpers.OperationHelpers.OperationResult.Companion.successResult
import uk.me.danielharman.kotlinspringbot.models.audit.AuditItem
import uk.me.danielharman.kotlinspringbot.repositories.AuditRepository
import java.lang.Integer.max

@Service
class AuditService(private val auditRepository: AuditRepository) {

    fun audit(discordId: String, administratorId: String, action: String): OperationResult<AuditItem?> = successResult(auditRepository.save(AuditItem(discordId, administratorId, action)))

    fun getAudit(id: String): OperationResult<AuditItem?> {
        val findById = auditRepository.findById(id)

        return if (findById.isEmpty)
            failResult("Audit not found")
        else
            successResult(findById.get())

    }

    fun getAudits(page: Int = 0, pageSize: Int = 25): OperationResult<List<AuditItem>?>
        = successResult(auditRepository.findAll(PageRequest.of(max(0, page), max(0, pageSize))).toList())


    fun getAuditsByDiscordId(discordId: String, page: Int = 0, pageSize: Int = 25): OperationResult<List<AuditItem>?>
        = successResult(auditRepository.getAllByDiscordId(discordId,
                PageRequest.of(max(0, page), max(0, pageSize), Sort.by(Sort.Order.desc("id")))).toList())


    fun getAuditsByAdministratorId(administratorId: String, page: Int = 0, pageSize: Int = 25): OperationResult<List<AuditItem>?>
        = successResult(auditRepository.getAllByAdministratorId(administratorId,
                PageRequest.of(max(0, page), max(0, pageSize), Sort.by(Sort.Order.desc("id")))).toList())

}