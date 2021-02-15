package uk.me.danielharman.kotlinspringbot.services

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.PrivateChannel
import net.dv8tion.jda.api.entities.TextChannel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.objects.DiscordObject
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.models.DiscordChannelEmbedMessage
import uk.me.danielharman.kotlinspringbot.models.DiscordChannelMessage

@Service
class DiscordService(
    private val guildService: GuildService,
    private val xkcdService: XkcdService,
) {

    private val logger = LoggerFactory.getLogger(DiscordService::class.java)

    fun sendLatestXkcd() {
        logger.info("[Discord Actor] Checking for new XKCD comic")

        val xkcdChannels = guildService.getXkcdChannels()

        if (xkcdChannels.isEmpty())
            return

        val last = xkcdService.getLast()
        val latestComic = xkcdService.getLatestComic()

        logger.info("[Discord Actor] XKCD last comic recorded #${last}. Current #${latestComic.num}")
        if (last == null || last.num < latestComic.num) {
            xkcdService.setLast(latestComic.num)
            for (channel in xkcdChannels) {
                sendChannelMessage(
                    DiscordChannelEmbedMessage(
                        Embeds.createXkcdComicEmbed(latestComic, "Latest comic"),
                        channel
                    )
                )
            }
        }
    }

    fun sendUserMessage(msg: String, userId: String) :PrivateChannel?  {
        val user = DiscordObject.jda.retrieveUserById(userId).complete()
        val privateChannel = user.openPrivateChannel().complete()
        privateChannel.sendMessage(msg).queue()
        return privateChannel
    }

    fun sendChannelMessage(msg: DiscordChannelMessage): TextChannel? {
        val channel = DiscordObject.jda.getTextChannelById(msg.channelId)
        channel?.sendMessage(msg.msg)?.queue() ?: logger.error("Could not send message $msg")
        return channel
    }

    fun sendChannelMessage(msg: DiscordChannelEmbedMessage): TextChannel? {
        val channel = DiscordObject.jda.getTextChannelById(msg.channelId)
        channel?.sendMessage(msg.msg)?.queue() ?: logger.error("Could not send message $msg")
        return channel
    }

    fun getGuild(id: String): Guild? = DiscordObject.jda.getGuildById(id)

    fun getBotName(): String = DiscordObject.jda.selfUser.name

}