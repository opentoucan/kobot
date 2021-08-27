package uk.me.danielharman.kotlinspringbot.services

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.PrivateChannel
import net.dv8tion.jda.api.entities.TextChannel
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.DiscordChannelEmbedMessage
import uk.me.danielharman.kotlinspringbot.models.DiscordChannelMessage
import uk.me.danielharman.kotlinspringbot.models.SpringGuild
import uk.me.danielharman.kotlinspringbot.objects.DiscordObject

@Service
class DiscordService(
    private val springGuildService: SpringGuildService,
    private val xkcdService: XkcdService,
    private val properties: KotlinBotProperties,
    private val commands: List<ISlashCommand>
) {

    private val logger = LoggerFactory.getLogger(DiscordService::class.java)

    fun sendLatestXkcd(): OperationResult<String, String> {
        logger.info("Checking for new XKCD comic")

        val xkcdChannels = springGuildService.getXkcdChannels() as Success

        if (xkcdChannels.value.isEmpty())
            return Success("Nothing to do")


        when (val latestComic = xkcdService.getLatestComic()) {
            is Failure -> {
                logger.error(latestComic.reason)
                return latestComic
            }
            is Success -> {
                when (val last = xkcdService.getLast()) {
                    is Failure -> {
                        logger.error(last.reason)
                        return last
                    }
                    is Success -> {
                        logger.info("XKCD last comic recorded #${last}. Current #${latestComic.value.num}")
                        if (last.value.num < latestComic.value.num) {
                            xkcdService.setLast(latestComic.value.num)
                            for (channel in xkcdChannels.value) {
                                sendChannelMessage(
                                    DiscordChannelEmbedMessage(
                                        Embeds.createXkcdComicEmbed(latestComic.value, "Latest comic"),
                                        channel
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        return Success(xkcdChannels.value.joinToString(" "))
    }

    fun sendUserMessage(msg: String, userId: String): PrivateChannel? {
        val user = DiscordObject.jda.retrieveUserById(userId).complete()
        val privateChannel = user.openPrivateChannel().complete()
        privateChannel.sendMessage(msg).queue()
        return privateChannel
    }

    fun sendChannelMessage(msg: DiscordChannelMessage): OperationResult<TextChannel, String> {
        val channel =
            DiscordObject.jda.getTextChannelById(msg.channelId) ?: return Failure("No such channel ${msg.channelId}")
        channel.sendMessage(msg.msg).queue()
        return Success(channel)
    }

    fun sendChannelMessage(msg: DiscordChannelEmbedMessage): OperationResult<TextChannel, String> {
        val channel =
            DiscordObject.jda.getTextChannelById(msg.channelId) ?: return Failure("No such channel ${msg.channelId}")
        channel.sendMessageEmbeds(msg.msg).queue()
        return Success(channel)
    }

    fun getGuild(id: String): OperationResult<Guild, String> {
        val guild = DiscordObject.jda.getGuildById(id) ?: return Failure("No such guild $id")
        return Success(guild)
    }

    fun getBotName(): OperationResult<String, String> = Success(DiscordObject.jda.selfUser.name)

    fun closeDiscordConnection(): OperationResult<String, String> {
        DiscordObject.teardown()
        return Success("Discord connection down at ${DateTime.now()}")
    }

    fun startDiscordConnection(): OperationResult<String, String> {
        if (!DiscordObject.initialised) {
            DiscordObject.init(properties, commands)
            return Success("Discord connection up at ${DiscordObject.startTime?.toString() ?: "????"}")
        }
        return Failure("Discord connection is already up. Started at ${DiscordObject.startTime?.toString()}")
    }

    fun getDiscordStartTime(): OperationResult<DateTime, String> {
        val startTime = DiscordObject.startTime ?: return Failure("Failed to get start time")
        return Success(startTime)
    }

    fun syncGuildsWithDb(): OperationResult<List<SpringGuild>, String> {
        logger.info("Syncing guilds with Database")
        val guilds = DiscordObject.jda.guilds
        val result = mutableListOf<SpringGuild>()
        for (guild in guilds) {
            when (val sg = springGuildService.createGuild(guild.id)) {
                is Failure -> {
                    logger.warn(sg.reason)
                }
                is Success -> {
                    result.add(sg.value)
                }
            }
        }
        return Success(result)
    }

}