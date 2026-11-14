package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.entities.User
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import uk.me.danielharman.kotlinspringbot.events.DiscordChannelMessageEvent
import uk.me.danielharman.kotlinspringbot.events.DiscordSlashCommandEvent

@ExtendWith(MockitoExtension::class)
internal class PingCommandTest {
    @InjectMocks private lateinit var pingCommand: PingCommand

    @Mock private lateinit var event: DiscordChannelMessageEvent

    @Test
    fun shouldPingOnChannelMessage() {
        val event = Mockito.mock(DiscordChannelMessageEvent::class.java)
        val author = Mockito.mock(User::class.java)
        `when`(event.author).thenReturn(author)
        `when`(author.asMention).thenReturn("1234")

        pingCommand.execute(event)

        Mockito.verify(event).reply("pong 1234")
    }

    @Test
    fun shouldPingOnSlashCommand() {
        val event = Mockito.mock(DiscordSlashCommandEvent::class.java)
        val author = Mockito.mock(User::class.java)
        `when`(event.author).thenReturn(author)
        `when`(author.asMention).thenReturn("1234")

        pingCommand.execute(event)

        Mockito.verify(event).reply("pong 1234")
    }
}
