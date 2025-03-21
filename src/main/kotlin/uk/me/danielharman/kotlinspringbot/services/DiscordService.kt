package uk.me.danielharman.kotlinspringbot.services

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.utils.FileUpload
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.DiscordChannelEmbedMessage
import uk.me.danielharman.kotlinspringbot.models.DiscordChannelMessage
import uk.me.danielharman.kotlinspringbot.models.SpringGuild
import uk.me.danielharman.kotlinspringbot.objects.DiscordObject
import uk.me.danielharman.kotlinspringbot.properties.KotlinBotProperties
import java.time.LocalDateTime

@Service
class DiscordService(
    private val springGuildService: SpringGuildService,
    private val properties: KotlinBotProperties,
    private val commands: List<ISlashCommand>,
) {
    private val logger = LoggerFactory.getLogger(DiscordService::class.java)

    fun sendUserMessage(
        msg: String,
        userId: String,
    ): PrivateChannel? {
        val user = DiscordObject.jda.retrieveUserById(userId).complete()
        val privateChannel = user.openPrivateChannel().complete()
        privateChannel.sendMessage(msg).queue()
        return privateChannel
    }

    fun sendChannelMessage(msg: DiscordChannelMessage): OperationResult<TextChannel, String> {
        val channel =
            DiscordObject.jda.getTextChannelById(msg.channelId)
                ?: return Failure("No such channel ${msg.channelId}")
        val fileUploads = mutableListOf<FileUpload>()
        for (file in msg.attachments) {
            fileUploads.add(FileUpload.fromData(file.content, file.fileName))
        }

        channel.sendMessage(msg.msg).addFiles(fileUploads).queue()
        return Success(channel)
    }

    fun sendChannelMessage(msg: DiscordChannelEmbedMessage): OperationResult<TextChannel, String> {
        val channel =
            DiscordObject.jda.getTextChannelById(msg.channelId)
                ?: return Failure("No such channel ${msg.channelId}")
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
        return Success("Discord connection down at ${LocalDateTime.now()}")
    }

    fun startDiscordConnection(): OperationResult<String, String> {
        if (!DiscordObject.initialised) {
            DiscordObject.init(properties, commands)
            return Success(
                "Discord connection up at ${DiscordObject.startTime?.toString() ?: "????"}",
            )
        }
        return Failure(
            "Discord connection is already up. Started at ${DiscordObject.startTime?.toString()}",
        )
    }

    fun getDiscordStartTime(): OperationResult<LocalDateTime, String> {
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
