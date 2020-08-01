package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.entities.VoiceChannel

interface VoiceCommand: Command{
    var voiceChannel: VoiceChannel?
}