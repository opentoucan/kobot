package uk.me.danielharman.kotlinspringbot.listeners

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.SelfUser
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import uk.me.danielharman.kotlinspringbot.commands.CommandPermission
import uk.me.danielharman.kotlinspringbot.commands.CommandRegistry
import uk.me.danielharman.kotlinspringbot.properties.KotlinBotProperties

internal class DirectMessageListenerTest {
    @Test
    fun shouldRejectPrivilegedDmFromNonAdmin() {
        val commandRegistry = mock(CommandRegistry::class.java)
        val properties = mock(KotlinBotProperties::class.java)
        val event = mock(MessageReceivedEvent::class.java)
        val message = mock(net.dv8tion.jda.api.entities.Message::class.java)
        val author = mock(User::class.java)
        val jda = mock(JDA::class.java)
        val selfUser = mock(SelfUser::class.java)
        val channel = mock(MessageChannelUnion::class.java)
        val response = mock(MessageCreateAction::class.java)

        `when`(properties.privilegedCommandPrefix).thenReturn("$")
        `when`(event.isFromGuild).thenReturn(false)
        `when`(event.isWebhookMessage).thenReturn(false)
        `when`(event.message).thenReturn(message)
        `when`(message.contentStripped).thenReturn("\$info")
        `when`(event.author).thenReturn(author)
        `when`(author.id).thenReturn("123")
        `when`(author.isBot).thenReturn(false)
        `when`(event.jda).thenReturn(jda)
        `when`(jda.selfUser).thenReturn(selfUser)
        `when`(selfUser.id).thenReturn("bot")
        `when`(event.channel).thenReturn(channel)
        `when`(channel.sendMessage("You are not an admin!")).thenReturn(response)
        `when`(commandRegistry.isAuthorized(CommandPermission.BOT_ADMIN, "123", null)).thenReturn(false)

        val listener = DirectMessageListener(commandRegistry, properties)

        listener.onMessageReceived(event)

        verify(commandRegistry).isAuthorized(CommandPermission.BOT_ADMIN, "123", null)
        verify(channel).sendMessage("You are not an admin!")
        verify(response).queue()
        verify(commandRegistry, never()).adminCommand("info")
    }
}