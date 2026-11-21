package uk.me.danielharman.kotlinspringbot.listeners.guildmessage

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.events.integration.MemePostedIntegrationEvent
import uk.me.danielharman.kotlinspringbot.events.integration.MemePostedIntegrationEventPublisher
import uk.me.danielharman.kotlinspringbot.helpers.EmojiCodes
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.JDAHelperFunctions.getAuthorIdFromMessageId
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.Meme
import uk.me.danielharman.kotlinspringbot.models.SpringGuild
import uk.me.danielharman.kotlinspringbot.properties.FeatureProperties
import uk.me.danielharman.kotlinspringbot.properties.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.services.CommandExecutionService
import uk.me.danielharman.kotlinspringbot.services.MemeService
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService
import java.awt.Color
import java.net.URI
import java.net.URISyntaxException
import java.util.Base64
import java.util.Locale

@Component
class MemeGuildMessageListener(
    private val properties: KotlinBotProperties,
    private val springGuildService: SpringGuildService,
    private val memeService: MemeService,
    private val memePostedIntegrationEventPublisher: MemePostedIntegrationEventPublisher,
    private val featureProperties: FeatureProperties,
    private val commandExecutionService: CommandExecutionService,
) : ListenerAdapter() {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        event.message.contentRaw
        if (
            event.message.contentStripped.startsWith(properties.commandPrefix) ||
            event.message.contentStripped.startsWith(properties.privilegedCommandPrefix) ||
            !event.isFromGuild ||
            event.author.isBot
        ) {
            return
        }

        val isDeafened = springGuildService.isChannelDeafened(event.guild.id, event.channel.id)

        val getMemeChannels = springGuildService.getMemeChannels(event.guild.id)
        var memeChannels = listOf<String>()
        if (getMemeChannels is Success) {
            memeChannels = getMemeChannels.value
        }

        if (memeChannels.contains(event.channel.id) && event.message.attachments.isNotEmpty()) {
            createMeme(event.message, event.guild.id, event.author.id, event.channel.id)

            val attachment = event.message.attachments[0]
            if (featureProperties.memeRepost && attachment.isImage) {
                val authorName = event.author.effectiveName
                val rgb = event.member?.color?.rgb ?: Color.WHITE.rgb
                val colour = "#${Integer.toHexString(rgb).substring(2)}"
                val avatarUrl = event.author.effectiveAvatarUrl
                val guildId = event.guild.id
                val channelId = event.channel.id
                val messageId = event.messageId

                // Downloading the avatar and image is slow, so keep it off JDA's event thread
                commandExecutionService.execute("meme repost for message $messageId") {
                    val avatar =
                        Base64
                            .getEncoder()
                            .encodeToString(URI(avatarUrl).toURL().openStream().use { it.readBytes() })
                    val image =
                        Base64
                            .getEncoder()
                            .encodeToString(URI(attachment.url).toURL().openStream().use { IOUtils.toByteArray(it) })

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
                }
            }
        }

        val words =
            event.message.contentStripped
                .lowercase(Locale.getDefault())
                .replace(Regex("[.!?,$\\\\-]"), "")
                .split(" ")
                .filter { s -> s.isNotBlank() }

        if (!isDeafened && words.size == 1 && words[0] == "lol") {
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
        if (message.attachments.isNotEmpty()) {
            message.addReaction(EmojiCodes.ThumbsUp).queue()
            message.addReaction(EmojiCodes.ThumbsDown).queue()
            memeService.saveMeme(
                Meme(
                    message.id,
                    guildId,
                    authorId,
                    message.attachments[0].url,
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

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (!event.isFromGuild || event.userId == event.jda.selfUser.id || event.user?.isBot == true) return

        val emoji = event.reaction.emoji

        val guild = getSpringGuild(event.guild.id, "onMessageReactionAdd") ?: return

        if (guild.memeChannels.contains(event.channel.id)) {
            if (
                event.userId ==
                getAuthorIdFromMessageId(
                    event.channel,
                    event.messageId,
                )
            ) {
                handleReactionByMemeAuthor(event, guild)
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

    // The poster of a meme can't vote on it, but can delete it (cross) or force-save it (check mark)
    private fun handleReactionByMemeAuthor(
        event: MessageReactionAddEvent,
        guild: SpringGuild,
    ) {
        val emoji = event.reaction.emoji
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
    }

    override fun onMessageReactionRemove(event: MessageReactionRemoveEvent) {
        if (!event.isFromGuild || event.userId == event.jda.selfUser.id) return

        val emoji = event.reaction.emoji
        val guild = getSpringGuild(event.guild.id, "onMessageReactionRemove") ?: return

        if (guild.memeChannels.contains(event.channel.id)) {
            if (
                event.userId ==
                getAuthorIdFromMessageId(
                    event.channel,
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

        val guild = getSpringGuild(event.guild.id, "onMessageDelete") ?: return

        if (guild.memeChannels.contains(event.channel.id)) {
            memeService.deleteMeme(guild.guildId, event.messageId)
        }
    }

    private fun getSpringGuild(
        guildId: String,
        context: String,
    ): SpringGuild? = when (val getGuild = springGuildService.getGuild(guildId)) {
        is Failure -> {
            logger.error("$context: ${getGuild.reason}")
            null
        }

        is Success -> getGuild.value
    }
}
