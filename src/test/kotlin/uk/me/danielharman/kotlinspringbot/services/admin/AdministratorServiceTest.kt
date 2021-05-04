package uk.me.danielharman.kotlinspringbot.services.admin

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.test.context.ActiveProfiles
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.SpringGuild
import uk.me.danielharman.kotlinspringbot.models.admin.Administrator
import uk.me.danielharman.kotlinspringbot.models.admin.enums.Role
import uk.me.danielharman.kotlinspringbot.repositories.admin.AdministratorRepository
import uk.me.danielharman.kotlinspringbot.services.DiscordService
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService
import java.util.*

@SpringBootTest
@EnableConfigurationProperties(value = [KotlinBotProperties::class])
@ActiveProfiles("test")
internal class AdministratorServiceTest {

    @InjectMocks
    lateinit var administratorService: AdministratorService

    @Mock
    lateinit var repository: AdministratorRepository

    @Mock
    lateinit var springGuildService: SpringGuildService

    @Mock
    lateinit var discordService: DiscordService

    @Mock
    lateinit var properties: KotlinBotProperties

    @Mock
    lateinit var mongoOperations: MongoOperations


    @Test
    fun shouldCreateAdmin() {
        val stubAdministrator = Administrator("123", setOf())
        val stubAdmin2 = Administrator("abc")
        Mockito.`when`(repository.findById("abc")).thenReturn(Optional.of(stubAdmin2))
        Mockito.`when`(repository.save(Mockito.any(Administrator::class.java))).thenReturn(stubAdministrator)

        val sut = administratorService.createBotAdministrator("abc", "123", setOf())

        assertTrue(sut.success)
        assertThat(
            sut.value!!, allOf(
                hasProperty("discordId", equalTo("123")),
                hasProperty("roles", emptyCollectionOf(Role::class.java))
            )
        )
    }

    @Test
    fun shouldRemoveAdmin() {
        Mockito.`when`(properties.primaryPrivilegedUserId).thenReturn("456")

        val removeAdmin = administratorService.removeBotAdministrator("1234")

        Mockito.verify(repository, times(1)).deleteByDiscordId("1234")

        assertThat(removeAdmin.success, equalTo(true))
    }

    @Test
    fun shouldNotRemovePrimaryAdmin() {
        Mockito.`when`(properties.primaryPrivilegedUserId).thenReturn("1234")

        val removeAdmin = administratorService.removeBotAdministrator("1234")

        Mockito.verify(repository, times(0)).deleteByDiscordId("1234")

        assertThat(removeAdmin.failure, equalTo(true))
    }

    @Test
    fun shouldGetBotAdministratorById() {
        val stubAdmin = Administrator("123", setOf())
        Mockito.`when`(repository.getByDiscordId("123")).thenReturn(stubAdmin)

        val result = administratorService.getBotAdministratorByDiscordId("123")

        Mockito.verify(repository, times(1)).getByDiscordId("123")

        assertThat(result.success, equalTo(true))
        assertThat(
            result.value!!, allOf(
                hasProperty("discordId", equalTo("123")),
                hasProperty("roles", emptyCollectionOf(Role::class.java))
            )
        )
    }

    @Test
    fun shouldAddSpringAdmin() {
        val stubSpringGuild = Success(SpringGuild("123"))
        stubSpringGuild.value.privilegedUsers = listOf("892")

        Mockito.`when`(springGuildService.getGuild("123")).thenReturn(stubSpringGuild)
        Mockito.`when`(springGuildService.addModerator("123", "456")).thenReturn(Success("Added 456"))

        val result = administratorService.addSpringGuildAdministrator("892", "123", "456")

        Mockito.verify(springGuildService, times(1)).getGuild("123")
        Mockito.verify(springGuildService, times(1)).addModerator("123", "456")

        assertThat(result.success, equalTo(true))
    }

    @Test
    fun shouldNotAddSpringAdminWithInsufficientPerms() {
        val stubSpringGuild = Success(SpringGuild("123"))

        Mockito.`when`(springGuildService.getGuild("123")).thenReturn(stubSpringGuild)

        val result = administratorService.addSpringGuildAdministrator("892", "123", "456")

        Mockito.verify(springGuildService, times(1)).getGuild("123")
        Mockito.verify(springGuildService, times(0)).addModerator("123", "456")

        assertThat(result.failure, equalTo(true))
    }

    @Test
    fun shouldRemoveSpringAdmin() {
        val stubSpringGuild = Success(SpringGuild("123"))
        stubSpringGuild.value.privilegedUsers = listOf("892")

        Mockito.`when`(springGuildService.getGuild("123")).thenReturn(stubSpringGuild)
        Mockito.`when`(springGuildService.removeModerator("123", "456")).thenReturn(Success("Removed 456"))

        val result = administratorService.removeSpringGuildAdministrator("892", "123", "456")

        Mockito.verify(springGuildService, times(1)).getGuild("123")
        Mockito.verify(springGuildService, times(1)).removeModerator("123", "456")

        assertThat(result.success, equalTo(true))
    }

    @Test
    fun shouldNotRemoveSpringAdminWithInsufficientPerms() {
        val stubSpringGuild = Success(SpringGuild("123"))

        Mockito.`when`(springGuildService.getGuild("123")).thenReturn(stubSpringGuild)

        val result = administratorService.removeSpringGuildAdministrator("892", "123", "456")

        Mockito.verify(springGuildService, times(1)).getGuild("123")
        Mockito.verify(springGuildService, times(0)).removeModerator("123", "456")

        assertThat(result.failure, equalTo(true))
    }

    @Test
    fun shouldGetSpringGuild() {
        val stubSpringGuild = Success(SpringGuild("123"))
        stubSpringGuild.value.privilegedUsers = listOf("892")

        Mockito.`when`(springGuildService.getGuild("123")).thenReturn(stubSpringGuild)

        val result = administratorService.getSpringGuild("892", "123")

        Mockito.verify(springGuildService, times(1)).getGuild("123")

        assertThat(result.success, equalTo(true))
    }

    @Test
    fun shouldNotGetSpringGuildWithInsufficientPerms() {
        val stubSpringGuild = Success(SpringGuild("123"))

        Mockito.`when`(springGuildService.getGuild("123")).thenReturn(stubSpringGuild)

        val result = administratorService.getSpringGuild("892", "123")

        Mockito.verify(springGuildService, times(1)).getGuild("123")

        assertThat(result.failure, equalTo(true))
    }

    @Test
    fun shouldDeleteSpringGuild() {
        val stubSpringGuild = Success(SpringGuild("123"))
        stubSpringGuild.value.privilegedUsers = listOf("892")

        Mockito.`when`(springGuildService.getGuild("123")).thenReturn(stubSpringGuild)
        Mockito.`when`(springGuildService.deleteSpringGuild("123")).thenReturn(Success("Delete"))

        val result = administratorService.deleteSpringGuild("892", "123")

        Mockito.verify(springGuildService, times(1)).getGuild("123")
        Mockito.verify(springGuildService, times(1)).deleteSpringGuild("123")

        assertThat(result.success, equalTo(true))
    }

    @Test
    fun shouldNotDeleteSpringGuildWithInsufficientPerms() {
        val stubSpringGuild = Success(SpringGuild("123"))

        Mockito.`when`(springGuildService.getGuild("123")).thenReturn(stubSpringGuild)

        val result = administratorService.getSpringGuild("892", "123")

        Mockito.verify(springGuildService, times(1)).getGuild("123")
        Mockito.verify(springGuildService, times(0)).deleteSpringGuild("123")

        assertThat(result.failure, equalTo(true))
    }


    @Test
    fun shouldSyncGuildAdmins(){
        val stubSpringGuilds = Success(listOf(SpringGuild("123"), SpringGuild("456")))

        val stubGuild1 = Mockito.mock(Guild::class.java)
        val owner1 = Mockito.mock(Member::class.java)
        val user1 = Mockito.mock(User::class.java)

        val stubGuild2 = Mockito.mock(Guild::class.java)
        val owner2 = Mockito.mock(Member::class.java)
        val user2 = Mockito.mock(User::class.java)

        Mockito.`when`(user1.id).thenReturn("8910")
        Mockito.`when`(owner1.user).thenReturn(user1)
        Mockito.`when`(stubGuild1.owner).thenReturn(owner1)

        Mockito.`when`(user2.id).thenReturn("8911")
        Mockito.`when`(owner2.user).thenReturn(user2)
        Mockito.`when`(stubGuild2.owner).thenReturn(owner2)

        Mockito.`when`(springGuildService.getGuildsWithoutModerators()).thenReturn(stubSpringGuilds)
        Mockito.`when`(discordService.getGuild("123")).thenReturn(stubGuild1)
        Mockito.`when`(discordService.getGuild("456")).thenReturn(stubGuild2)

        val result = administratorService.syncGuildAdmins()

        Mockito.verify(springGuildService, times(1)).addModerator("123","8910")
        Mockito.verify(springGuildService, times(1)).addModerator("456","8911")
        Mockito.verify(discordService, times(2)).sendUserMessage(Mockito.anyString(), Mockito.anyString())

        assertThat(result.success, equalTo(true))
    }

}