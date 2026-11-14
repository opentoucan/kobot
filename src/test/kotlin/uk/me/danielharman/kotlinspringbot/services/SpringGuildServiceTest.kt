package uk.me.danielharman.kotlinspringbot.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import uk.me.danielharman.kotlinspringbot.helpers.assertFailure
import uk.me.danielharman.kotlinspringbot.helpers.assertSuccess
import uk.me.danielharman.kotlinspringbot.models.SpringGuild
import uk.me.danielharman.kotlinspringbot.repositories.GuildRepository

internal class SpringGuildServiceTest {
    private val repo: GuildRepository = mock(GuildRepository::class.java)
    private val service: SpringGuildService = SpringGuildService(repo)

    @Test
    fun shouldCreateIfNotExistsGuild() {
        val guild = SpringGuild("1234")
        `when`(repo.findByGuildId("1234")).thenReturn(null)
        `when`(repo.save(any(SpringGuild::class.java))).thenReturn(guild)

        val result = assertSuccess(service.createGuild("1234"))

        assertEquals("1234", result.value.guildId)
    }

    @Test
    fun shouldNotCreateIfExistsGuild() {
        val guild = SpringGuild("1234")
        `when`(repo.findByGuildId("1234")).thenReturn(guild)

        assertFailure(service.createGuild("1234"))
    }

    @Test
    fun shouldDeleteSpringGuild() {
        val guild = SpringGuild("1234")
        `when`(repo.findByGuildId("1234")).thenReturn(guild)

        val result = assertSuccess(service.deleteSpringGuild("1234"))

        assertEquals(guild.id, result.value)
    }

    @Test
    fun shouldSetVol() {
        val guild = SpringGuild("1234")
        guild.volume = 50
        `when`(repo.findByGuildId("1234")).thenReturn(null)
        `when`(repo.save(any(SpringGuild::class.java))).thenReturn(guild)
        `when`(repo.setGuildVolume("1234", 20)).thenReturn(guild.apply { volume = 20 })

        val result = assertSuccess(service.setVol("1234", 20))

        assertEquals(20, result.value)
    }

    @Test
    fun shouldGetVolIfGuildExists() {
        val guild = SpringGuild("1234")
        guild.volume = 69
        `when`(repo.findByGuildId("1234")).thenReturn(guild)

        val result = assertSuccess(service.getVol("1234"))

        assertEquals(69, result.value)
    }

    @Test
    fun shouldFailVolIfGuildDoesNotExist() {
        `when`(repo.findByGuildId("1234")).thenReturn(null)

        assertFailure(service.getVol("1234"))
    }

    @Test
    fun shouldReturnSuccessIfModerator() {
        val guild = SpringGuild("1234")
        guild.privilegedUsers = listOf("1234")
        `when`(repo.findByGuildId("1234")).thenReturn(guild)

        assertSuccess(service.isModerator("1234", "1234"))
    }

    @Test
    fun shouldReturnFailureIfNotModerator() {
        val guild = SpringGuild("1234")
        `when`(repo.findByGuildId("1234")).thenReturn(guild)

        assertFailure(service.isModerator("1234", "1234"))
    }

    @Test
    fun shouldAddModerator() {
        val guild = SpringGuild("1234")
        `when`(repo.findByGuildId("1234")).thenReturn(null)
        `when`(repo.save(any(SpringGuild::class.java))).thenReturn(guild)
        `when`(repo.addModeratorId("1234", "1234")).thenReturn(guild.apply { privilegedUsers = listOf("1234") })

        val result = assertSuccess(service.addModerator("1234", "1234"))

        assertEquals("1234", result.value)
    }

    @Test
    fun shouldRemoveModerator() {
        val guild = SpringGuild("1234")
        guild.privilegedUsers = listOf("1234", "4567", "8910")
        `when`(repo.findByGuildId("1234")).thenReturn(guild)
        `when`(repo.removeModeratorId("1234", "4567")).thenReturn(guild.apply { privilegedUsers = listOf("1234", "8910") })

        val result = assertSuccess(service.removeModerator("1234", "4567"))

        assertEquals("4567", result.value)
    }

    @Test
    fun shouldAddMemeChannel() {
        val guild = SpringGuild("1234")
        `when`(repo.findByGuildId("1234")).thenReturn(null)
        `when`(repo.save(any(SpringGuild::class.java))).thenReturn(guild)
        `when`(repo.addMemeChannelId("1234", "456")).thenReturn(guild.apply { memeChannels = listOf("456") })

        val result = assertSuccess(service.addMemeChannel("1234", "456"))

        assertEquals("456", result.value)
    }

    @Test
    fun shouldRemoveMemeChannel() {
        val guild = SpringGuild("1234")
        guild.memeChannels = listOf("1234", "456", "789")
        `when`(repo.findByGuildId("1234")).thenReturn(guild)
        `when`(repo.removeMemeChannelId("1234", "456")).thenReturn(guild.apply { memeChannels = listOf("1234", "789") })

        val result = assertSuccess(service.removeMemeChannel("1234", "456"))

        assertEquals("456", result.value)
    }

    @Test
    fun shouldGetMemeChannels() {
        val guild = SpringGuild("1234")
        guild.memeChannels = listOf("1", "2", "3", "4")
        `when`(repo.findByGuildId("1234")).thenReturn(guild)

        val result = assertSuccess(service.getMemeChannels("1234"))

        assertEquals(listOf("1", "2", "3", "4"), result.value)
    }

    @Test
    fun shouldDeafenChannel() {
        val guild = SpringGuild("1234")
        `when`(repo.findByGuildId("1234")).thenReturn(guild)
        `when`(repo.addDeafenChannelId("1234", "456")).thenReturn(guild.apply { deafenedChannels = listOf("456") })

        val result = assertSuccess(service.deafenChannel("1234", "456"))

        assertEquals("456", result.value)
    }

    @Test
    fun shouldUnDeafenChannel() {
        val guild = SpringGuild("1234")
        guild.deafenedChannels = listOf("1", "2", "3")
        `when`(repo.findByGuildId("1234")).thenReturn(guild)
        `when`(repo.removeDeafenChannelId("1234", "2")).thenReturn(guild.apply { deafenedChannels = listOf("1", "3") })

        val result = assertSuccess(service.unDeafenChannel("1234", "2"))

        assertEquals("2", result.value)
    }

    @Test
    fun shouldGetDeafenedChannels() {
        val guild = SpringGuild("1234")
        guild.deafenedChannels = listOf("1", "2", "3")
        `when`(repo.findByGuildId("1234")).thenReturn(guild)

        val result = assertSuccess(service.getDeafenedChannels("1234"))

        assertEquals(listOf("1", "2", "3"), result.value)
    }
}
