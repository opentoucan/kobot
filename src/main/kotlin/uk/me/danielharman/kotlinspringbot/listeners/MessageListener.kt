package uk.me.danielharman.kotlinspringbot.listeners

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.helpers.EmojiCodes
import uk.me.danielharman.kotlinspringbot.helpers.JDAHelperFunctions.getAuthorIdFromMessageId
import uk.me.danielharman.kotlinspringbot.models.Meme
import uk.me.danielharman.kotlinspringbot.services.AdminCommandService
import uk.me.danielharman.kotlinspringbot.services.CommandService
import uk.me.danielharman.kotlinspringbot.services.GuildService
import uk.me.danielharman.kotlinspringbot.services.MemeService
import java.util.regex.Pattern


class MessageListener(private val guildService: GuildService,
                      private val adminCommandService: AdminCommandService,
                      private val commandService: CommandService,
                      private val properties: KotlinBotProperties,
                      private val memeService: MemeService,
                      playerManager: AudioPlayerManager = DefaultAudioPlayerManager()) : ListenerAdapter() {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    init {
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        logger.info("Joined guild ${event.guild.name}")

        val guild = guildService.getGuild(event.guild.id)
        if (guild == null || guild.privilegedUsers.isEmpty()) {
            val owner = event.guild.retrieveOwner().complete()
            logger.info("Adding ${owner.nickname} as admin of guild")
            guildService.addPrivileged(event.guild.id, owner.id)
        }

        val defaultChannel = event.guild.defaultChannel ?: return

        if (defaultChannel.canTalk()) {
            defaultChannel.sendTyping().queue()
            defaultChannel.sendMessage(":wave:").queue()
        }
    }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {

        if (event.userId == event.jda.selfUser.id || event.user?.isBot == true)
            return

        if (event.reactionEmote.isEmoji) {
            val emoji = event.reactionEmote.asCodepoints
            val guild = guildService.getGuild(event.guild.id) ?: return

            if (guild.memeChannels.contains(event.channel.id)) {

                if (event.userId == getAuthorIdFromMessageId(event.reaction.textChannel, event.messageId)) {
                    val message = event.retrieveMessage().complete()
                    when (emoji) {
                        EmojiCodes.ThumbsDown, EmojiCodes.ThumbsUp -> {
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
                            createMeme(message, guild.guildId, message.author.id, message.channel.id, true)
                            message.clearReactions(EmojiCodes.CheckMark).queue()
                        }
                    }
                    return
                }
                else if(emoji == EmojiCodes.Cross){
                    event.reaction.removeReaction().queue()
                }

                //Thumbs up
                if (emoji == EmojiCodes.ThumbsUp) {
                    if (!memeService.addUpvote(guild.guildId, event.messageId, event.userId)) logger.error("[MessageListener] Failed to upvote")
                }
                //Thumbs down
                else if (emoji == EmojiCodes.ThumbsDown) {
                    if (!memeService.addDownvote(guild.guildId, event.messageId, event.userId)) logger.error("[MessageListener] Failed to downvote")
                }
            }
        }
    }

    override fun onMessageReactionRemove(event: MessageReactionRemoveEvent) {

        if (event.userId == event.jda.selfUser.id)
            return

        if (event.reactionEmote.isEmoji) {
            val emoji = event.reactionEmote.asCodepoints
            val guild = guildService.getGuild(event.guild.id) ?: return

            if (guild.memeChannels.contains(event.channel.id)) {

                if (event.userId == getAuthorIdFromMessageId(event.reaction.textChannel, event.messageId))
                    return

                //Thumbs up
                if (emoji == EmojiCodes.ThumbsUp) {
                    memeService.removeUpvote(guild.guildId, event.messageId, event.userId)
                }
                //Thumbs down
                else if (emoji == EmojiCodes.ThumbsDown) {
                    memeService.removeDownvote(guild.guildId, event.messageId, event.userId)

                }
            }
        }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {

        val guild = guildService.getGuild(event.guild.id) ?: return

        if (guild.memeChannels.contains(event.channel.id)) {
            memeService.deleteMeme(guild.guildId, event.messageId)
        }
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {

        val author = event.author
        val message = event.message
        val guild = event.guild
        val member : Member?
        try {
            member = guild.retrieveMember(author).complete()
        }catch (e: ErrorResponseException){
            logger.error("Failed to retrieve $author while handling message $message")
            return
        }

        logger.debug("[${guild.name}] #${event.channel.name} <${member?.nickname ?: author.asTag}>: ${message.contentDisplay}")

        if (author.isBot)
            return

        val isDeafened = guildService.getDeafenedChannels(guild.id).contains(event.channel.id)

        if (!isDeafened && event.message.isMentioned(event.jda.selfUser, Message.MentionType.USER)) {
            val emotesByName = guild.getEmotesByName("piing", true)
            if (emotesByName.size >= 1)
                message.addReaction(emotesByName[0]).queue()
            else
                message.addReaction("U+1F621").queue()
        }

        when {
            message.contentStripped.startsWith(properties.commandPrefix) -> {
                if (!isDeafened)
                    runCommand(event)
            }
            message.contentStripped.startsWith(properties.privilegedCommandPrefix) -> {
                runAdminCommand(event)
            }
            else -> {

                if (guildService.getMemeChannels(event.guild.id).contains(event.channel.id)) {
                    createMeme(event.message, event.guild.id, event.author.id, event.channel.id)
                }

                val words = message.contentStripped
                        .toLowerCase()
                        .replace(Regex("[.!?,$\\\\-]"), "")
                        .split(" ")
                        .filter { s -> s.isNotBlank() }

                if (!isDeafened && words.size == 1 && words[0] == "lol") {
                    event.message.addReaction(EmojiCodes.Rofl).queue()
                }

                guildService.updateUserCount(guild.id, author.id, words.size)
            }
        }
    }

    private var urlPattern: Pattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL)

    private fun createMeme(message: Message, guildId: String, authorId: String, channelId: String, force: Boolean = false) {
        if (message.attachments.isNotEmpty()) {
            message.addReaction(EmojiCodes.ThumbsUp).queue()
            message.addReaction(EmojiCodes.ThumbsDown).queue()
            message.addReaction(EmojiCodes.Cross).queue()
            memeService.saveMeme(Meme(message.id, guildId, authorId, message.attachments[0].url, channelId, Meme.UrlType.Image))
        } else if (force) {
            message.addReaction(EmojiCodes.ThumbsUp).queue()
            message.addReaction(EmojiCodes.ThumbsDown).queue()
            message.addReaction(EmojiCodes.Cross).queue()
            memeService.saveMeme(Meme(message.id, guildId, authorId, message.jumpUrl, channelId, Meme.UrlType.Link))
        } else {
            var url: String? = null
            val matcher = urlPattern.matcher(message.contentRaw)
            while (matcher.find()) {
                url = message.contentRaw.slice(matcher.start(1) until matcher.end())
                break
            }
            if (url != null && (url.contains("youtube.com/watch?")
                            || url.contains("youtu.be")
                            || url.contains("i.reddit")
                            || url.contains("twitter.com")
                            || url.contains("facebook.com"))
            ) {
                message.addReaction(EmojiCodes.ThumbsUp).queue()
                message.addReaction(EmojiCodes.ThumbsDown).queue()
                message.addReaction(EmojiCodes.Cross).queue()
                memeService.saveMeme(Meme(message.id, guildId, authorId, url, channelId, Meme.UrlType.Link))
            }
        }
    }

    private fun runCommand(event: GuildMessageReceivedEvent) {

        if (event.author.id == event.jda.selfUser.id || event.author.isBot) {
            logger.info("Not running command as author is me or a bot")
            return
        }

        val cmd = event.message.contentStripped.split(" ")[0].removePrefix(properties.commandPrefix)
        val command = commandService.getCommand(cmd)
        command.execute(event)
    }

    private fun runAdminCommand(event: GuildMessageReceivedEvent) {

        val cmd = event.message.contentStripped.split(" ")[0].removePrefix(properties.privilegedCommandPrefix)
        val channel = event.channel

        if (event.author.id != properties.primaryPrivilegedUserId
                && !guildService.isPrivileged(event.guild.id, event.author.id)) {
            channel.sendMessage("You are not an admin!").queue()
            return
        }

        val command = adminCommandService.getCommand(cmd)
        command.execute(event)

    }

}