package uk.me.danielharman.kotlinspringbot.listeners.guildmessage

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.events.integration.MemePostedIntegrationEvent
import uk.me.danielharman.kotlinspringbot.events.integration.MemePostedIntegrationEventPublisher
import uk.me.danielharman.kotlinspringbot.helpers.EmojiCodes
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.JDAHelperFunctions.getAuthorIdFromMessageId
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.Meme
import uk.me.danielharman.kotlinspringbot.properties.FeatureProperties
import uk.me.danielharman.kotlinspringbot.properties.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.services.MemeService
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService
import java.awt.Color
import java.net.URI
import java.net.URISyntaxException
import java.util.Base64
import java.util.Locale

private const val CONNECTION_TIMEOUT_MILLIS = 5_000
private const val READ_TIMEOUT_MILLIS = 5_000
private const val MAX_IMAGE_BYTES = 8 * 1024 * 1024
private const val MAX_AVATAR_BYTES = 1024 * 1024

@Component
class MemeGuildMessageListener(
    private val properties: KotlinBotProperties,
    private val springGuildService: SpringGuildService,
    private val memeService: MemeService,
    private val memePostedIntegrationEventPublisher: MemePostedIntegrationEventPublisher,
    private val featureProperties: FeatureProperties,
    private val taskExecutor: TaskExecutor,
) : ListenerAdapter() {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (
            event.message.contentStripped.startsWith(properties.commandPrefix) ||
            event.message.contentStripped.startsWith(properties.privilegedCommandPrefix) ||
            !event.isFromGuild ||
            event.isWebhookMessage ||
            event.author.isBot
        ) {
            return
        }

        val attachment = event.message.attachments.firstOrNull()
        if (attachment != null) {
            val memeChannels = when (val result = springGuildService.getMemeChannels(event.guild.id)) {
                is Failure -> emptyList()
                is Success -> result.value
            }

            if (event.channel.id in memeChannels) {
                createMeme(event.message, event.guild.id, event.author.id, event.channel.id)

                if (attachment.isImage && featureProperties.memeRepost) {
                    scheduleMemeRepost(
                        attachment.url,
                        event.author.effectiveAvatarUrl,
                        event.author.effectiveName,
                        event.member?.colors?.primary?.rgb ?: Color.WHITE.rgb,
                        event.guild.id,
                        event.channel.id,
                        event.messageId,
                    )
                }
            }
        }

        val words =
            event.message.contentStripped
                .lowercase(Locale.getDefault())
                .replace(Regex("[.!?,$\\\\-]"), "")
                .split(" ")
                .filter { s -> s.isNotBlank() }

        if (
            words.size == 1 &&
            words[0] == "lol" &&
            !springGuildService.isChannelDeafened(event.guild.id, event.channel.id)
        ) {
            event.message.addReaction(EmojiCodes.Rofl).queue()
        }

        springGuildService.updateUserCount(event.guild.id, event.author.id, words.size)
    }

    private fun createMeme(
        message: Message,
        guildId: String,
        authorId: String,
        channelId: String,
        force: Boolean = false,
    ) {
        val attachment = message.attachments.firstOrNull()
        if (attachment != null) {
            if (!attachment.isImage) return

            message.addReaction(EmojiCodes.ThumbsUp).queue()
            message.addReaction(EmojiCodes.ThumbsDown).queue()
            memeService.saveMeme(
                Meme(
                    message.id,
                    guildId,
                    authorId,
                    attachment.url,
                    channelId,
                    Meme.UrlType.Image,
                ),
            )
        } else {
            try {
                val url = if (force) message.jumpUrl else URI(message.contentRaw).toURL().toString()
                message.addReaction(EmojiCodes.ThumbsUp).queue()
                message.addReaction(EmojiCodes.ThumbsDown).queue()
                memeService.saveMeme(
                    Meme(message.id, guildId, authorId, url, channelId, Meme.UrlType.Link),
                )
            } catch (ex: URISyntaxException) {
                logger.info("Invalid URL in message: ${ex.message}")
            }
        }
    }

    private fun scheduleMemeRepost(
        imageUrl: String,
        avatarUrl: String,
        authorName: String,
        authorColor: Int,
        guildId: String,
        channelId: String,
        messageId: String,
    ) {
        try {
            taskExecutor.execute {
                try {
                    val avatar = Base64.getEncoder().encodeToString(downloadBytes(avatarUrl, MAX_AVATAR_BYTES))
                    val image = Base64.getEncoder().encodeToString(downloadBytes(imageUrl, MAX_IMAGE_BYTES))
                    val colour = "#${Integer.toHexString(authorColor).substring(2)}"

                    memePostedIntegrationEventPublisher.publish(
                        MemePostedIntegrationEvent(
                            authorName,
                            colour,
                            avatar,
                            image,
                            guildId,
                            channelId,
                            messageId,
                        ),
                    )
                } catch (exception: Exception) {
                    logger.warn("Failed to repost image for message $messageId", exception)
                }
            }
        } catch (exception: RuntimeException) {
            logger.warn("Could not schedule image repost for message $messageId", exception)
        }
    }

    private fun downloadBytes(url: String, maxBytes: Int): ByteArray {
        val connection = URI(url).toURL().openConnection().apply {
            connectTimeout = CONNECTION_TIMEOUT_MILLIS
            readTimeout = READ_TIMEOUT_MILLIS
            useCaches = false
        }

        return connection.getInputStream().use { input ->
            val bytes = input.readNBytes(maxBytes + 1)
            require(bytes.size <= maxBytes) { "Downloaded file exceeds the $maxBytes byte limit" }
            bytes
        }
    }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (!event.isFromGuild || event.userId == event.jda.selfUser.id || event.user?.isBot == true) return

        val emoji = event.reaction.emoji

        val getGuild = springGuildService.getGuild(event.guild.id)
        if (getGuild is Failure) {
            logger.error("onMessageReactionAdd: ${getGuild.reason}")
            return
        }
        val guild = (getGuild as Success).value

        if (guild.memeChannels.contains(event.channel.id)) {
            if (
                event.userId ==
                getAuthorIdFromMessageId(
                    event.reaction.channel.asTextChannel(),
                    event.messageId,
                )
            ) {
                val message = event.retrieveMessage().complete()
                when (emoji) {
                    EmojiCodes.ThumbsDown,
                    EmojiCodes.ThumbsUp,
                    -> {
                        val user = event.user ?: return
                        logger.info("[Message Listener] Removing reaction by posting user")
                        event.reaction.removeReaction(user).queue()
                    }

                    EmojiCodes.Cross -> {
                        logger.info("Deleting meme by user request")
                        memeService.deleteMeme(event.guild.id, event.messageId)
                        message.clearReactions(EmojiCodes.ThumbsUp).queue()
                        message.clearReactions(EmojiCodes.ThumbsDown).queue()
                        message.clearReactions(EmojiCodes.Cross).queue()
                    }

                    EmojiCodes.CheckMark -> {
                        createMeme(
                            message,
                            guild.guildId,
                            message.author.id,
                            message.channel.id,
                            true,
                        )
                        message.clearReactions(EmojiCodes.CheckMark).queue()
                    }
                }
                return
            } else if (emoji == EmojiCodes.Cross) {
                event.user?.let { event.reaction.removeReaction(it).queue() }
            }

            if (emoji == EmojiCodes.ThumbsUp) {
                if (!memeService.addUpvote(guild.guildId, event.messageId, event.userId)) {
                    logger.error("[MessageListener] Failed to upvote")
                }
            } else if (emoji == EmojiCodes.ThumbsDown) {
                if (!memeService.addDownvote(guild.guildId, event.messageId, event.userId)) {
                    logger.error("[MessageListener] Failed to downvote")
                }
            }
        }
    }

    override fun onMessageReactionRemove(event: MessageReactionRemoveEvent) {
        if (!event.isFromGuild || event.userId == event.jda.selfUser.id || event.user?.isBot == true) return

        val emoji = event.reaction.emoji
        val getGuild = springGuildService.getGuild(event.guild.id)
        if (getGuild is Failure) {
            logger.error("onMessageReactionRemove: ${getGuild.reason}")
            return
        }
        val guild = (getGuild as Success).value

        if (guild.memeChannels.contains(event.channel.id)) {
            if (
                event.userId ==
                getAuthorIdFromMessageId(
                    event.reaction.channel.asTextChannel(),
                    event.messageId,
                )
            ) {
                return
            }

            if (emoji == EmojiCodes.ThumbsUp) {
                memeService.removeUpvote(guild.guildId, event.messageId, event.userId)
            } else if (emoji == EmojiCodes.ThumbsDown) {
                memeService.removeDownvote(guild.guildId, event.messageId, event.userId)
            }
        }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        if (!event.isFromGuild) return

        val getGuild = springGuildService.getGuild(event.guild.id)
        if (getGuild is Failure) {
            logger.error("onMessageDelete: ${getGuild.reason}")
            return
        }
        val guild = (getGuild as Success).value

        if (guild.memeChannels.contains(event.channel.id)) {
            memeService.deleteMeme(guild.guildId, event.messageId)
        }
    }
}
