package uk.me.danielharman.kotlinspringbot.services

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.PrivateChannel
import net.dv8tion.jda.api.entities.TextChannel
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.objects.DiscordObject
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.OperationHelpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.OperationHelpers.OperationResult.Companion.failResult
import uk.me.danielharman.kotlinspringbot.helpers.OperationHelpers.OperationResult.Companion.successResult
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.DiscordChannelEmbedMessage
import uk.me.danielharman.kotlinspringbot.models.DiscordChannelMessage

@Service
class DiscordService(
    private val springGuildService: SpringGuildService,
    private val xkcdService: XkcdService,
    private val properties: KotlinBotProperties
) {

    private val logger = LoggerFactory.getLogger(DiscordService::class.java)

    fun sendLatestXkcd() {
        logger.info("[Discord Actor] Checking for new XKCD comic")

        val xkcdChannels = springGuildService.getXkcdChannels() as Success

        if (xkcdChannels.value.isEmpty())
            return

        val last = xkcdService.getLast()
        val latestComic = xkcdService.getLatestComic()

        logger.info("[Discord Actor] XKCD last comic recorded #${last}. Current #${latestComic.num}")
        if (last == null || last.num < latestComic.num) {
            xkcdService.setLast(latestComic.num)
            for (channel in xkcdChannels.value) {
                sendChannelMessage(
                    DiscordChannelEmbedMessage(
                        Embeds.createXkcdComicEmbed(latestComic, "Latest comic"),
                        channel
                    )
                )
            }
        }
    }

    fun sendUserMessage(msg: String, userId: String): PrivateChannel? {
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

    fun closeDiscordConnection(): OperationResult<String?> {
        DiscordObject.teardown()
        return successResult("Discord connection down at ${DateTime.now()}")
    }

    fun startDiscordConnection(): OperationResult<String?> {
        if (!DiscordObject.initialised) {
            DiscordObject.init(properties)
            return successResult("Discord connection up at ${DiscordObject.startTime?.toString() ?: "????"}")
        }
        return failResult("Discord connection is already up. Started at ${DiscordObject.startTime?.toString()}")
    }

    fun getDiscordStartTime(): OperationResult<DateTime?> = successResult(DiscordObject.startTime)

}