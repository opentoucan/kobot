package uk.me.danielharman.kotlinspringbot.events

import net.dv8tion.jda.api.entities.Mentions
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import uk.me.danielharman.kotlinspringbot.models.CommandParameter

internal class DiscordChannelMessageEventTest {
    private fun eventWithContent(content: String): DiscordChannelMessageEvent {
        val jdaEvent = mock(MessageReceivedEvent::class.java)
        val message = mock(Message::class.java)
        val mentions = mock(Mentions::class.java)
        `when`(jdaEvent.message).thenReturn(message)
        `when`(jdaEvent.channel).thenReturn(mock(MessageChannelUnion::class.java))
        `when`(message.contentStripped).thenReturn(content)
        `when`(message.author).thenReturn(mock(User::class.java))
        `when`(message.attachments).thenReturn(listOf())
        `when`(message.mentions).thenReturn(mentions)
        `when`(mentions.users).thenReturn(listOf())
        return DiscordChannelMessageEvent(jdaEvent)
    }

    @Test
    fun shouldNotMutateSharedParameterDefinition() {
        val definition = CommandParameter(0, "volume", CommandParameter.ParamType.Long, "Volume", true)

        val parsed = eventWithContent("!vol 50").getParamValue(definition)

        assertEquals(50L, parsed.asLong())
        assertNull(definition.value)
        assertFalse(definition.error)
    }

    @Test
    fun shouldKeepValuesIndependentAcrossInvocations() {
        val definition = CommandParameter(0, "volume", CommandParameter.ParamType.Long, "Volume", true)

        val first = eventWithContent("!vol 50").getParamValue(definition)
        val second = eventWithContent("!vol 80").getParamValue(definition)

        assertEquals(50L, first.asLong())
        assertEquals(80L, second.asLong())
    }
}
