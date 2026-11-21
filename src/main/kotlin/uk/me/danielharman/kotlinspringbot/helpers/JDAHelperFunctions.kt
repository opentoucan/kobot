package uk.me.danielharman.kotlinspringbot.helpers

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.guild.GenericGuildEvent

// Discord's request-guild-members gateway call accepts at most 100 user ids
const val MEMBER_REQUEST_LIMIT = 100

object JDAHelperFunctions {
    /**
     * Fetches the members with the given ids, batching them into requests of at most
     * [MEMBER_REQUEST_LIMIT]. Ids that aren't members of the guild are absent from the result.
     */
    fun retrieveMembersById(
        guild: Guild,
        ids: Collection<String>,
    ): Map<String, Member> = ids
        .distinct()
        .chunked(MEMBER_REQUEST_LIMIT)
        .flatMap { chunk -> guild.retrieveMembersByIds(chunk.map { it.toLong() }).get() }
        .associateBy { it.id }

    fun getBotVoiceChannel(event: GenericGuildEvent): VoiceChannel? = event.guild
        .retrieveMemberById(event.jda.selfUser.id)
        .complete()
        ?.voiceState
        ?.channel
        ?.asVoiceChannel()

    fun getAuthorIdFromMessageId(
        channel: MessageChannel?,
        msgId: String,
    ): String = channel
        ?.retrieveMessageById(msgId)
        ?.complete()
        ?.author
        ?.id ?: ""

    @Deprecated("Use DiscordService")
    fun getChannelName(
        jda: JDA,
        id: String,
    ): String = jda.getGuildChannelById(id)?.name ?: id
}
