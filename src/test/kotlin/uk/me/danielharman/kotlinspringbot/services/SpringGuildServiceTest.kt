package uk.me.danielharman.kotlinspringbot.services

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.me.danielharman.kotlinspringbot.helpers.assertFailure
import uk.me.danielharman.kotlinspringbot.helpers.assertSuccess
import uk.me.danielharman.kotlinspringbot.models.SpringGuild
import uk.me.danielharman.kotlinspringbot.repositories.GuildRepository

@SpringBootTest
@ActiveProfiles("test")
internal class SpringGuildServiceTest(
    @Autowired private val service: SpringGuildService,
    @Autowired private val repo: GuildRepository
) {

    @AfterEach
    fun afterEach() {
        repo.deleteAll()
    }

    @Test
    fun shouldCreateIfNotExistsGuild() {
        assertSuccess(service.createGuild("1234"))
        assertNotNull(repo.findByGuildId("1234"))
    }

    @Test
    fun shouldNotCreateIfExistsGuild() {
        repo.save(SpringGuild("1234"))
        assertFailure(service.createGuild("1234"))
    }

    @Test
    fun shouldUpdateWordCount() {

        val stubGuild = SpringGuild("1234")

        repo.save(stubGuild)

        assertSuccess(service.updateUserCount("1234", "1234", 1))

        val result = repo.findByGuildId("1234")
        assertNotNull(result)
        assertEquals(1, result?.userWordCounts?.get("1234") ?: 0)
    }

    @Test
    fun shouldGetPaginatedGuilds() {
        for (i in 1..100) {
            repo.save(SpringGuild("$i"))
        }

        val result = assertSuccess(service.getGuilds(15, 1))

        assertEquals(15, result.value.size)
        for (i in 0..14) {
            assertEquals("${i + 16}", result.value[i].guildId)
        }
    }

    @Test
    fun shouldSetVol() {
        repo.save(SpringGuild("1234"))

        assertSuccess(service.setVol("1234", 20))

        val guild = repo.findByGuildId("1234")
        assertNotNull(guild)
        assertEquals(20, guild?.volume ?: -1)
    }

    @Test
    fun shouldGetVolIfGuildExists() {
        val stub = SpringGuild("1234")
        stub.volume = 69
        repo.save(stub)

        val result = assertSuccess(service.getVol("1234"))

        assertEquals(69, result.value)
    }

    @Test
    fun shouldFailVolIfGuildDoesNotExist() {
        assertFailure(service.getVol("1234"))
    }

    @Test
    fun shouldReturnSuccessIfModerator() {
        val stub = SpringGuild("1234")
        stub.privilegedUsers = listOf("1234")
        repo.save(stub)
        assertSuccess(service.isModerator("1234", "1234"))
    }

    @Test
    fun shouldReturnFailureIfNotModerator() {
        val stub = SpringGuild("1234")
        repo.save(stub)
        assertFailure(service.isModerator("1234", "1234"))
    }

    @Test
    fun shouldAddModerator() {
        val stub = SpringGuild("1234")
        repo.save(stub)

        assertSuccess(service.addModerator("1234", "1234"))

        val guild = repo.findByGuildId("1234")
        assertNotNull(guild)
        assertThat(guild?.privilegedUsers, containsInAnyOrder("1234"))
    }

    @Test
    fun shouldRemoveModerator() {
        val stub = SpringGuild("1234")
        stub.privilegedUsers = listOf("1234", "4567", "8910")
        repo.save(stub)

        assertSuccess(service.removeModerator("1234", "4567"))

        val guild = repo.findByGuildId("1234")
        assertNotNull(guild)
        assertThat(guild?.privilegedUsers, containsInAnyOrder("1234", "8910"))
        assertThat(guild?.privilegedUsers, not(containsInAnyOrder("4567")))
    }

}