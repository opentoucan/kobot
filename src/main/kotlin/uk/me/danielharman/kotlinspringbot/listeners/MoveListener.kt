package uk.me.danielharman.kotlinspringbot.listeners

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class MoveListener : ListenerAdapter() {
    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        if (event.channelLeft == null || event.channelJoined == null) return

        if (event.jda.selfUser.id == event.member.id) {
            event.channelLeft?.members?.forEach { m ->
                m.guild.moveVoiceMember(m, event.channelJoined).queue()
            }
            event.jda.removeEventListener(this)
            event.guild.audioManager.closeAudioConnection()
        }
    }
}
