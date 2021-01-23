package uk.me.danielharman.kotlinspringbot.services

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

    fun sendChannelMessage(msg: DiscordChannelMessage) {
        DiscordObject.jda.getTextChannelById(msg.channelId)?.sendMessage(msg.msg)?.queue()
            ?: logger.error("Could not send message $msg")
    }

    fun sendChannelMessage(msg: DiscordChannelEmbedMessage) {
        DiscordObject.jda.getTextChannelById(msg.channelId)?.sendMessage(msg.msg)?.queue()
            ?: logger.error("Could not send message $msg")
    }

}