package uk.me.danielharman.kotlinspringbot.services.admin

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.models.admin.Administrator
import uk.me.danielharman.kotlinspringbot.models.admin.enums.Role
import uk.me.danielharman.kotlinspringbot.repositories.admin.AdministratorRepository

@SpringBootTest
@EnableConfigurationProperties(value = [KotlinBotProperties::class])
@ActiveProfiles("test")
internal class AdministratorServiceTest(@Autowired val administratorService: AdministratorService,
                                        @Autowired val repository: AdministratorRepository) {

    @BeforeEach
    fun setUp() {

    }

    @AfterEach
    fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun shouldCreateAdmin(){

        assertTrue( administratorService.createAdmin("123", setOf()).success)

        val byDiscordId: Administrator? = repository.getByDiscordId("123")

        assertNotNull(byDiscordId)
        assertThat(byDiscordId!!, allOf(
                hasProperty("discordId", equalTo("123")),
                hasProperty("roles", emptyCollectionOf(Role::class.java))
        ))
    }

    @Test
    fun shouldNotRemovePrimaryAdmin(){
        repository.save(Administrator("1234"))
        val removeAdmin = administratorService.removeAdmin("1234")
        assertThat(removeAdmin.failure, equalTo(true))
    }

}