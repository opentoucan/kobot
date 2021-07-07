package uk.me.danielharman.kotlinspringbot.listeners

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.MessageAction
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.factories.CommandFactory
import uk.me.danielharman.kotlinspringbot.factories.ModeratorCommandFactory
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.SpringGuild
import uk.me.danielharman.kotlinspringbot.services.DiscordActionService
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService
import uk.me.danielharman.kotlinspringbot.services.MemeService

@SpringBootTest
@EnableConfigurationProperties(value = [KotlinBotProperties::class])
@ActiveProfiles("test")
internal class GuildMessageListenerTest {

    @InjectMocks
    private lateinit var listener: GuildMessageListener

    @Mock
    private lateinit var springGuildService: SpringGuildService

    @Mock
    private lateinit var moderatorCommandFactory: ModeratorCommandFactory

    @Mock
    private lateinit var commandFactory: CommandFactory

    @Mock
    private lateinit var properties: KotlinBotProperties

    @Mock
    private lateinit var memeService: MemeService

    @Mock
    private lateinit var discordService: DiscordActionService

    @Test
    fun onGuildJoin() {

        val event = mock(GuildJoinEvent::class.java)
        val guild = mock(Guild::class.java)
        val springGuild = mock(SpringGuild::class.java)
        val ownerRequest = mock(RestAction::class.java) as RestAction<Member>
        val sendTyping = mock(RestAction::class.java) as RestAction<Void>
        val messageRequest = mock(MessageAction::class.java)
        val owner = mock(Member::class.java)
        val defaultChannel = mock(TextChannel::class.java)

        `when`(springGuild.privilegedUsers).thenReturn(listOf())
        `when`(event.guild).thenReturn(guild)
        `when`(guild.id).thenReturn("123")
        `when`(guild.name).thenReturn("Test guild")
        `when`(springGuildService.getGuild("123")).thenReturn(Success(springGuild))
        `when`(guild.retrieveOwner()).thenReturn(ownerRequest)
        `when`(ownerRequest.complete()).thenReturn(owner)
        `when`(owner.id).thenReturn("123")
        `when`(owner.nickname).thenReturn("Name")
        `when`(springGuildService.addModerator("123", "123")).thenReturn(Success(""))

        `when`(guild.defaultChannel).thenReturn(defaultChannel)
        `when`(defaultChannel.canTalk()).thenReturn(true)
        `when`(defaultChannel.sendTyping()).thenReturn(sendTyping)
        `when`(defaultChannel.sendMessage(":wave:")).thenReturn(messageRequest)

        listener.onGuildJoin(event)

        verify(springGuildService, times(1)).getGuild("123")
        verify(springGuild, times(1)).privilegedUsers
        verify(guild, times(1)).retrieveOwner()
        verify(ownerRequest, times(1)).complete()
        verify(springGuildService, times(1)).addModerator("123", "123")
        verify(guild, times(1)).defaultChannel
        verify(defaultChannel, times(1)).canTalk()
        verify(defaultChannel, times(1)).sendTyping()
        verify(sendTyping, times(1)).queue()
        verify(defaultChannel, times(1)).sendMessage(":wave:")
        verify(messageRequest, times(1)).queue()
    }


}