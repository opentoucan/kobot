package uk.me.danielharman.kotlinspringbot.services

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
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
    fun shouldDeleteSpringGuild() {
        repo.save(SpringGuild("1234"))
        assertSuccess(service.deleteSpringGuild("1234"))
        assertNull(repo.findByGuildId("1234"))
    }

    @Test
    fun shouldGetAllGuilds() {
        val guilds = listOf(SpringGuild("1"), SpringGuild("2"), SpringGuild("3"))
        repo.saveAll(guilds)
        assertSuccess(service.getGuilds())
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

    @Test
    fun shouldAddMemeChannel() {
        val stub = SpringGuild("1234")
        repo.save(stub)

        assertSuccess(service.addMemeChannel("1234", "456"))

        val guild = repo.findByGuildId("1234")
        assertNotNull(guild)
        assertEquals(1, guild?.memeChannels?.size)
        assertThat(guild?.memeChannels, contains("456"))
    }

    @Test
    fun shouldRemoveMemeChannel() {
        val stub = SpringGuild("1234")
        stub.memeChannels = listOf("1234", "456", "789")
        repo.save(stub)

        assertSuccess(service.removeMemeChannel("1234", "456"))

        val guild = repo.findByGuildId("1234")
        assertNotNull(guild)
        assertEquals(2, guild?.memeChannels?.size)
        assertThat(guild?.memeChannels, containsInAnyOrder("1234", "789"))
        assertThat(guild?.memeChannels, not(containsInAnyOrder("456")))
    }

    @Test
    fun shouldGetMemeChannels() {
        val stub = SpringGuild("1234")
        stub.memeChannels = listOf("1", "2", "3", "4")
        repo.save(stub)

        val result = assertSuccess(service.getMemeChannels("1234"))
        assertThat(result.value, containsInAnyOrder("1", "2", "3", "4"))
    }

    @Test
    fun shouldDeafenChannel() {
        repo.save(SpringGuild("1234"))
        assertSuccess(service.deafenChannel("1234", "456"))
        val guild = repo.findByGuildId("1234")
        assertNotNull(guild)
        assertThat(guild?.deafenedChannels, containsInAnyOrder("456"))
    }

    @Test
    fun shouldUnDeafenChannel() {
        val stub = SpringGuild("1234")
        stub.deafenedChannels = listOf("1", "2", "3")
        repo.save(stub)
        assertSuccess(service.unDeafenChannel("1234", "2"))
        val guild = repo.findByGuildId("1234")
        assertNotNull(guild)
        assertThat(guild?.deafenedChannels, containsInAnyOrder("1", "3"))
        assertThat(guild?.deafenedChannels, not(containsInAnyOrder("2")))
    }

    @Test
    fun shouldGetDeafenedChannels() {
        val stub = SpringGuild("1234")
        stub.deafenedChannels = listOf("1", "2", "3")
        repo.save(stub)

        val result = assertSuccess(service.getDeafenedChannels("1234"))
        assertEquals(3, result.value.size)
        assertThat(result.value, containsInAnyOrder("1", "2", "3"))
    }

    @Test
    fun getGuildsWithoutModerators() {
        val stub1 = SpringGuild("1")
        stub1.privilegedUsers = listOf("1")
        repo.save(stub1)
        val stub2 = SpringGuild("2")
        repo.save(stub2)
        val stub3 = SpringGuild("3")
        stub3.privilegedUsers = listOf("3", "4")
        repo.save(stub3)

        val result = assertSuccess(service.getGuildsWithoutModerators())
        assertEquals(1, result.value.size)
        assertThat(result.value, containsInAnyOrder(stub2))
    }

}