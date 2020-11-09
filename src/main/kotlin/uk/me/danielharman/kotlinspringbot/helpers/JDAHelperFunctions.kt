package uk.me.danielharman.kotlinspringbot.helpers

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.guild.GenericGuildEvent

object JDAHelperFunctions {

    fun getBotVoiceChannel(event: GenericGuildEvent): VoiceChannel? =
            event.guild.getMemberById(event.jda.selfUser.id)?.voiceState?.channel

    fun getAuthorIdFromMessageId(textChannel: TextChannel?, msgId: String): String =
            textChannel?.retrieveMessageById(msgId)?.complete()?.author?.id ?: ""

    fun getChannelName(jda: JDA, id: String): String = jda.getGuildChannelById(id)?.name ?: id

}