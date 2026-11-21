package uk.me.danielharman.kotlinspringbot.services.admin

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.emptyCollectionOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasProperty
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.times
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.test.context.ActiveProfiles
import uk.me.danielharman.kotlinspringbot.TestcontainersConfiguration
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.helpers.assertFailure
import uk.me.danielharman.kotlinspringbot.helpers.assertSuccess
import uk.me.danielharman.kotlinspringbot.models.SpringGuild
import uk.me.danielharman.kotlinspringbot.models.admin.Administrator
import uk.me.danielharman.kotlinspringbot.models.admin.enums.Role
import uk.me.danielharman.kotlinspringbot.properties.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.repositories.admin.AdministratorRepository
import uk.me.danielharman.kotlinspringbot.services.DiscordService
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService
import java.util.Optional

@SpringBootTest
@Import(TestcontainersConfiguration::class)
@ExtendWith(MockitoExtension::class)
@EnableConfigurationProperties(value = [KotlinBotProperties::class])
@ActiveProfiles("test")
internal class AdministratorServiceTest {
    @InjectMocks lateinit var administratorService: AdministratorService

    @Mock lateinit var repository: AdministratorRepository

    @Mock lateinit var springGuildService: SpringGuildService

    @Mock lateinit var discordService: DiscordService

    @Mock lateinit var properties: KotlinBotProperties

    @Mock lateinit var mongoOperations: MongoOperations

    private fun stubAdmin(
        id: String,
        discordId: String,
        roles: Set<Role> = setOf(),
    ): Administrator {
        val admin = Administrator(discordId, roles)
        admin.id = id
        return admin
    }

    @Test
    fun shouldCreateAdmin() {
        val stubAdministrator = Administrator("123", setOf())
        val caller = stubAdmin("abc", "999", setOf(Role.ManageAdmin))
        Mockito.`when`(repository.findById("abc")).thenReturn(Optional.of(caller))
        Mockito
            .`when`(repository.save(Mockito.any(Administrator::class.java)))
            .thenReturn(stubAdministrator)

        val sut = administratorService.createBotAdministrator("abc", "123", setOf())

        val result = assertSuccess(sut)
        assertThat(
            result.value,
            allOf(
                hasProperty("discordId", equalTo("123")),
                hasProperty("roles", emptyCollectionOf(Role::class.java)),
            ),
        )
    }

    @Test
    fun shouldNotCreateAdminWithoutManageAdminRole() {
        val caller = stubAdmin("abc", "999")
        Mockito.`when`(repository.findById("abc")).thenReturn(Optional.of(caller))

        val sut = administratorService.createBotAdministrator("abc", "123", setOf())

        assertFailure(sut)
        Mockito.verify(repository, times(0)).save(Mockito.any(Administrator::class.java))
    }

    @Test
    fun shouldRemoveAdmin() {
        val caller = stubAdmin("abc", "999", setOf(Role.ManageAdmin))
        Mockito.`when`(repository.findById("abc")).thenReturn(Optional.of(caller))
        Mockito.`when`(properties.primaryPrivilegedUserId).thenReturn("456")

        val removeAdmin = administratorService.removeBotAdministrator("abc", "1234")

        Mockito.verify(repository, times(1)).deleteByDiscordId("1234")

        assertSuccess(removeAdmin)
    }

    @Test
    fun shouldNotRemoveAdminWithoutManageAdminRole() {
        val caller = stubAdmin("abc", "999")
        Mockito.`when`(repository.findById("abc")).thenReturn(Optional.of(caller))

        val removeAdmin = administratorService.removeBotAdministrator("abc", "1234")

        Mockito.verify(repository, times(0)).deleteByDiscordId("1234")

        assertFailure(removeAdmin)
    }

    @Test
    fun shouldNotRemovePrimaryAdmin() {
        val caller = stubAdmin("abc", "999", setOf(Role.Primary))
        Mockito.`when`(repository.findById("abc")).thenReturn(Optional.of(caller))
        Mockito.`when`(properties.primaryPrivilegedUserId).thenReturn("1234")

        val removeAdmin = administratorService.removeBotAdministrator("abc", "1234")

        Mockito.verify(repository, times(0)).deleteByDiscordId("1234")

        assertFailure(removeAdmin)
    }

    @Test
    fun shouldAddRoleWhenCallerIdentifiedByDiscordId() {
        val caller = stubAdmin("abc", "999", setOf(Role.ManageAdmin))
        val target = stubAdmin("def", "123")
        Mockito.`when`(repository.getByDiscordId("999")).thenReturn(caller)
        Mockito.`when`(repository.getByDiscordId("123")).thenReturn(target)
        Mockito.`when`(repository.findById("abc")).thenReturn(Optional.of(caller))

        assertSuccess(administratorService.addRole("999", "123", Role.Logging))
    }

    @Test
    fun shouldNotAddRoleWithoutManageAdminRole() {
        val caller = stubAdmin("abc", "999")
        val target = stubAdmin("def", "123")
        Mockito.`when`(repository.getByDiscordId("999")).thenReturn(caller)
        Mockito.`when`(repository.getByDiscordId("123")).thenReturn(target)
        Mockito.`when`(repository.findById("abc")).thenReturn(Optional.of(caller))

        assertFailure(administratorService.addRole("999", "123", Role.Logging))
    }

    @Test
    fun shouldRemoveRoleWhenCallerIdentifiedByDiscordId() {
        val caller = stubAdmin("abc", "999", setOf(Role.ManageAdmin))
        val target = stubAdmin("def", "123", setOf(Role.Logging))
        Mockito.`when`(repository.getByDiscordId("999")).thenReturn(caller)
        Mockito.`when`(repository.getByDiscordId("123")).thenReturn(target)
        Mockito.`when`(repository.findById("abc")).thenReturn(Optional.of(caller))

        assertSuccess(administratorService.removeRole("999", "123", Role.Logging))
    }

    @Test
    fun shouldGetBotAdministratorById() {
        val stubAdmin = Administrator("123", setOf())
        Mockito.`when`(repository.getByDiscordId("123")).thenReturn(stubAdmin)

        val sut = administratorService.getBotAdministratorByDiscordId("123")

        Mockito.verify(repository, times(1)).getByDiscordId("123")

        val result = assertSuccess(sut)
        assertThat(
            result.value,
            allOf(
                hasProperty("discordId", equalTo("123")),
                hasProperty("roles", emptyCollectionOf(Role::class.java)),
            ),
        )
    }

    @Test
    fun shouldAddSpringAdmin() {
        val stubSpringGuild = Success(SpringGuild("123"))
        stubSpringGuild.value.privilegedUsers = listOf("892")

        Mockito.`when`(springGuildService.getGuild("123")).thenReturn(stubSpringGuild)
        Mockito
            .`when`(springGuildService.addModerator("123", "456"))
            .thenReturn(Success("Added 456"))

        val result = administratorService.addSpringGuildAdministrator("892", "123", "456")

        Mockito.verify(springGuildService, times(1)).getGuild("123")
        Mockito.verify(springGuildService, times(1)).addModerator("123", "456")

        assertSuccess(result)
    }

    @Test
    fun shouldNotAddSpringAdminWithInsufficientPerms() {
        val stubSpringGuild = Success(SpringGuild("123"))

        Mockito.`when`(springGuildService.getGuild("123")).thenReturn(stubSpringGuild)

        val result = administratorService.addSpringGuildAdministrator("892", "123", "456")

        Mockito.verify(springGuildService, times(1)).getGuild("123")
        Mockito.verify(springGuildService, times(0)).addModerator("123", "456")

        assertFailure(result)
    }

    @Test
    fun shouldRemoveSpringAdmin() {
        val stubSpringGuild = Success(SpringGuild("123"))
        stubSpringGuild.value.privilegedUsers = listOf("892")

        Mockito.`when`(springGuildService.getGuild("123")).thenReturn(stubSpringGuild)
        Mockito
            .`when`(springGuildService.removeModerator("123", "456"))
            .thenReturn(Success("Removed 456"))

        val result = administratorService.removeSpringGuildAdministrator("892", "123", "456")

        Mockito.verify(springGuildService, times(1)).getGuild("123")
        Mockito.verify(springGuildService, times(1)).removeModerator("123", "456")

        assertSuccess(result)
    }

    @Test
    fun shouldNotRemoveSpringAdminWithInsufficientPerms() {
        val stubSpringGuild = Success(SpringGuild("123"))

        Mockito.`when`(springGuildService.getGuild("123")).thenReturn(stubSpringGuild)

        val result = administratorService.removeSpringGuildAdministrator("892", "123", "456")

        Mockito.verify(springGuildService, times(1)).getGuild("123")
        Mockito.verify(springGuildService, times(0)).removeModerator("123", "456")

        assertFailure(result)
    }

    @Test
    fun shouldGetSpringGuild() {
        val stubSpringGuild = Success(SpringGuild("123"))
        stubSpringGuild.value.privilegedUsers = listOf("892")

        Mockito.`when`(springGuildService.getGuild("123")).thenReturn(stubSpringGuild)

        val result = administratorService.getSpringGuild("892", "123")

        Mockito.verify(springGuildService, times(1)).getGuild("123")

        assertSuccess(result)
    }

    @Test
    fun shouldNotGetSpringGuildWithInsufficientPerms() {
        val stubSpringGuild = Success(SpringGuild("123"))

        Mockito.`when`(springGuildService.getGuild("123")).thenReturn(stubSpringGuild)

        val result = administratorService.getSpringGuild("892", "123")

        Mockito.verify(springGuildService, times(1)).getGuild("123")

        assertFailure(result)
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

        assertSuccess(result)
    }

    @Test
    fun shouldNotDeleteSpringGuildWithInsufficientPerms() {
        val stubSpringGuild = Success(SpringGuild("123"))

        Mockito.`when`(springGuildService.getGuild("123")).thenReturn(stubSpringGuild)

        val result = administratorService.deleteSpringGuild("892", "123")

        Mockito.verify(springGuildService, times(1)).getGuild("123")
        Mockito.verify(springGuildService, times(0)).deleteSpringGuild("123")

        assertFailure(result)
    }

    @Test
    fun shouldSyncGuildAdmins() {
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
        Mockito.`when`(discordService.getGuild("123")).thenReturn(Success(stubGuild1))
        Mockito.`when`(discordService.getGuild("456")).thenReturn(Success(stubGuild2))

        val result = administratorService.syncGuildAdmins()

        Mockito.verify(springGuildService, times(1)).addModerator("123", "8910")
        Mockito.verify(springGuildService, times(1)).addModerator("456", "8911")
        Mockito
            .verify(discordService, times(2))
            .sendUserMessage(Mockito.anyString(), Mockito.anyString())

        assertSuccess(result)
    }
}
