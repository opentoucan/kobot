package uk.me.danielharman.kotlinspringbot.helpers

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.events.guild.GenericGuildEvent

object JDAHelperFunctions {

    fun getBotVoiceChannel(event: GenericGuildEvent): VoiceChannel? =
            event.guild.retrieveMemberById(event.jda.selfUser.id).complete()?.voiceState?.channel?.asVoiceChannel()

    fun getAuthorIdFromMessageId(textChannel: TextChannel?, msgId: String): String =
            textChannel?.retrieveMessageById(msgId)?.complete()?.author?.id ?: ""

    @Deprecated("Use DiscordService")
    fun getChannelName(jda: JDA, id: String): String = jda.getGuildChannelById(id)?.name ?: id

}