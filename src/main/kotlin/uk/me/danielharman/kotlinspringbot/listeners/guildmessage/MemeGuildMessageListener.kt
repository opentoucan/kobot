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
import uk.me.danielharman.kotlinspringbot.properties.FeatureProperties
import uk.me.danielharman.kotlinspringbot.properties.KotlinBotProperties
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

            val memeFileUrl = URI(event.message.attachments[0].url).toURL()

            val content = memeFileUrl.openConnection().contentType
            if (
                content.startsWith("image") &&
                event.message.attachments.isNotEmpty() &&
                featureProperties.memeRepost
            ) {
                val authorName = event.author.effectiveName
                val rgb = event.member?.color?.rgb ?: Color.WHITE.rgb
                val colour = "#${Integer.toHexString(rgb).substring(2)}"
                val avatar =
                    Base64
                        .getEncoder()
                        .encodeToString(
                            URI(event.author.effectiveAvatarUrl).toURL().openStream().readBytes(),
                        )

                memePostedIntegrationEventPublisher.publish(
                    MemePostedIntegrationEvent(
                        authorName,
                        colour,
                        avatar,
                        Base64
                            .getEncoder()
                            .encodeToString(IOUtils.toByteArray((memeFileUrl).openStream())),
                        event.guild.id,
                        event.channel.id,
                        event.messageId,
                    ),
                )
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
        if (event.userId == event.jda.selfUser.id || event.user?.isBot == true) return

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
        if (event.userId == event.jda.selfUser.id) return

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
