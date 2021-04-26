package uk.me.danielharman.kotlinspringbot.services.audit

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.me.danielharman.kotlinspringbot.models.audit.AuditItem
import uk.me.danielharman.kotlinspringbot.repositories.AuditRepository

@SpringBootTest
@ActiveProfiles("test")
class AuditServiceTest(@Autowired private val auditService: AuditService, @Autowired private val repository: AuditRepository) {

    @AfterEach
    fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun shouldAudit() {

        assertTrue(auditService.audit("123", "123", "action").success)

        val findAll = repository.findAll()

        assertThat(findAll, containsInAnyOrder(
                allOf(
                        hasProperty("discordId", equalTo("123")),
                        hasProperty("administratorId", equalTo("123")),
                        hasProperty("action", equalTo("action"))
                )
            )
        )

    }

    @Test
    fun shouldGetAuditsByDiscordIds(){
        repository.save(AuditItem("123", "123", ""))
        repository.save(AuditItem("123", "123", ""))
        repository.save(AuditItem("123", "123", ""))

        val auditsByDiscordId = auditService.getAuditsByDiscordId("123")

        assertTrue(auditsByDiscordId.success, auditsByDiscordId.message)

        assertThat(auditsByDiscordId.value!!.size, equalTo(3))
    }

    @Test
    fun shouldGetAuditsByAdminIds(){
        repository.save(AuditItem("123", "123", ""))
        repository.save(AuditItem("123", "123", ""))
        repository.save(AuditItem("123", "123", ""))

        val auditsByDiscordId = auditService.getAuditsByAdministratorId("123")

        assertTrue(auditsByDiscordId.success, auditsByDiscordId.message)

        assertThat(auditsByDiscordId.value!!.size, equalTo(3))
    }

}