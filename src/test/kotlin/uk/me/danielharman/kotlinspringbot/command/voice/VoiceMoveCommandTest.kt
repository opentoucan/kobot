package uk.me.danielharman.kotlinspringbot.command.voice

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.GuildVoiceState
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.managers.AudioManager
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.services.DiscordActionService

internal class VoiceMoveCommandTest {
    @Test
    fun `permission failure joining voice channel is reported and does not enable moving`() {
        val discordActionService = mock(DiscordActionService::class.java)
        val command = VoiceMoveCommand(discordActionService)
        val event = mock(DiscordMessageEvent::class.java)
        val guild = mock(Guild::class.java)
        val author = mock(User::class.java)
        val member = mock(Member::class.java)
        val voiceState = mock(GuildVoiceState::class.java)
        val audioChannel = mock(AudioChannelUnion::class.java)
        val audioManager = mock(AudioManager::class.java)
        val messageChannel = mock(MessageChannel::class.java)

        `when`(event.guild).thenReturn(guild)
        `when`(event.author).thenReturn(author)
        `when`(event.channel).thenReturn(messageChannel)
        `when`(guild.audioManager).thenReturn(audioManager)
        `when`(guild.getMember(author)).thenReturn(member)
        `when`(member.voiceState).thenReturn(voiceState)
        `when`(voiceState.channel).thenReturn(audioChannel)
        `when`(audioManager.isConnected).thenReturn(false)
        doThrow(mock(InsufficientPermissionException::class.java))
            .`when`(audioManager)
            .openAudioConnection(audioChannel)

        command.execute(event)

        verify(event).reply("I don't have permission to join.")
        verify(discordActionService, never()).enableVoiceMove()
    }
}
