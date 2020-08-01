package uk.me.danielharman.kotlinspringbot.helpers

import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.guild.GenericGuildEvent

object BotHelperFunctions {

    fun getBotVoiceChannel(event: GenericGuildEvent): VoiceChannel? =
            event.guild.getMemberById(event.jda.selfUser.id)?.voiceState?.channel
}