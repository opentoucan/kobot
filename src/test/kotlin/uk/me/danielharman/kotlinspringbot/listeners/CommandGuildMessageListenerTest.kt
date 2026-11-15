package uk.me.danielharman.kotlinspringbot.listeners

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import uk.me.danielharman.kotlinspringbot.commands.CommandRegistry
import uk.me.danielharman.kotlinspringbot.listeners.guildmessage.CommandGuildMessageListener
import uk.me.danielharman.kotlinspringbot.properties.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

internal class CommandGuildMessageListenerTest {
    @Test
    fun shouldIgnoreDirectMessagesBeforeAccessingGuildServices() {
        val springGuildService = mock(SpringGuildService::class.java)
        val commandRegistry = mock(CommandRegistry::class.java)
        val event = mock(MessageReceivedEvent::class.java)
        `when`(event.isFromGuild).thenReturn(false)

        val listener =
            CommandGuildMessageListener(
                commandRegistry,
                mock(KotlinBotProperties::class.java),
                springGuildService,
            )

        listener.onMessageReceived(event)

        verifyNoInteractions(springGuildService, commandRegistry)
    }
}