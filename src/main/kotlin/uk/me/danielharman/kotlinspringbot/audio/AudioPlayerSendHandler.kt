package uk.me.danielharman.kotlinspringbot.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.Buffer
import java.nio.ByteBuffer

private const val BUFFER_SIZE = 1024

class AudioPlayerSendHandler(
    private val audioPlayer: AudioPlayer,
) : AudioSendHandler {
    private val buffer: ByteBuffer = ByteBuffer.allocate(BUFFER_SIZE)
    private val frame: MutableAudioFrame = MutableAudioFrame()

    init {
        frame.setBuffer(buffer)
    }

    override fun provide20MsAudio(): ByteBuffer {
        (buffer as Buffer).flip()
        return buffer
    }

    override fun canProvide(): Boolean = audioPlayer.provide(frame)

    override fun isOpus(): Boolean = true
}
