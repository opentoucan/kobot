package uk.me.danielharman.kotlinspringbot.listeners

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class MoveListener : ListenerAdapter() {
    override fun onGuildVoiceMove(event: GuildVoiceMoveEvent) {
        if (event.jda.selfUser.id == event.member.id) {
            event.channelLeft.members.forEach { m -> m.guild.moveVoiceMember(m, event.channelJoined).queue() }
            event.jda.removeEventListener(this)
            event.guild.audioManager.closeAudioConnection()
        }
    }
}