package uk.me.danielharman.kotlinspringbot.command

import io.kotest.core.spec.style.FunSpec
import io.mockk.*
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent

class VoiceMoveCommandTests: FunSpec({
    val mockListener = VoiceMoveCommand.MoveListener()
    val voiceEvent = mockk<GuildVoiceMoveEvent>{jda}

    test("Calls event listener"){
        every {} just runs
        mockListener.onGuildVoiceMove(voiceEvent)
        verify(exactly = 1){mockListener.onGuildVoiceMove(voiceEvent)}
    }
})
