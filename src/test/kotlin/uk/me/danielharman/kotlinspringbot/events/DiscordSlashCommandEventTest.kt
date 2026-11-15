package uk.me.danielharman.kotlinspringbot.events

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import uk.me.danielharman.kotlinspringbot.models.CommandParameter

internal class DiscordSlashCommandEventTest {
    @Test
    fun parsedValuesAndErrorsAreLocalToEachInvocation() {
        val definition = CommandParameter(0, "name", CommandParameter.ParamType.Word, "Name", required = true)

        val missingValue = eventWithOption(null).getParamValue(definition)
        val providedValue = eventWithOption("Ada").getParamValue(definition)

        assertTrue(missingValue.error)
        assertFalse(providedValue.error)
        assertEquals("Ada", providedValue.asString())
        assertEquals("name", definition.name)
    }

    private fun eventWithOption(value: String?): DiscordSlashCommandEvent {
        val interaction = mock(SlashCommandInteraction::class.java)
        val option = mock(OptionMapping::class.java)
        `when`(interaction.options).thenReturn(if (value == null) emptyList() else listOf(option))
        `when`(interaction.channel).thenReturn(mock(MessageChannelUnion::class.java))
        `when`(interaction.user).thenReturn(mock(User::class.java))
        if (value != null) {
            `when`(option.name).thenReturn("name")
            `when`(option.type).thenReturn(OptionType.STRING)
            `when`(option.asString).thenReturn(value)
        }
        return DiscordSlashCommandEvent(interaction)
    }
}