package uk.me.danielharman.kotlinspringbot.listeners

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.joda.time.DateTime
import uk.me.danielharman.kotlinspringbot.ApplicationLogger.logger
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.helpers.EmojiCodes
import uk.me.danielharman.kotlinspringbot.helpers.JDAHelperFunctions.getAuthorIdFromMessageId
import uk.me.danielharman.kotlinspringbot.models.Meme
import uk.me.danielharman.kotlinspringbot.services.AdminCommandService
import uk.me.danielharman.kotlinspringbot.services.CommandService
import uk.me.danielharman.kotlinspringbot.services.GuildService
import uk.me.danielharman.kotlinspringbot.services.MemeService


class MessageListener(private val guildService: GuildService,
                      private val adminCommandService: AdminCommandService,
                      private val commandService: CommandService,
                      private val properties: KotlinBotProperties,
                      private val memeService: MemeService,
                      playerManager: AudioPlayerManager = DefaultAudioPlayerManager()) : ListenerAdapter() {
    init {
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)
    }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {

        if (event.userId == event.jda.selfUser.id)
            return

        if (event.reactionEmote.isEmoji) {
            val emoji = event.reactionEmote.asCodepoints
            val guild = guildService.getGuild(event.guild.id) ?: return

            if (guild.memeChannelId == event.channel.id) {

                if (event.userId == getAuthorIdFromMessageId(event.reaction.textChannel, event.messageId)) {
                    if (emoji == EmojiCodes.ThumbsDown || emoji == EmojiCodes.ThumbsUp) {
                        val user = event.user ?: return
                        logger.info("[Message Listener] Removing reaction by posting user")
                        event.reaction.removeReaction(user).queue()
                    }
                    return
                }

                //Thumbs up
                if (emoji == EmojiCodes.ThumbsUp) {
                    memeService.incUpvotes(event.messageId)
                }
                //Thumbs down
                else if (emoji == EmojiCodes.ThumbsDown) {
                    memeService.incDownvotes(event.messageId)
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

            if (guild.memeChannelId == event.channel.id) {

                if (event.userId == getAuthorIdFromMessageId(event.reaction.textChannel, event.messageId))
                    return

                //Thumbs up
                if (emoji == EmojiCodes.ThumbsUp) {
                    memeService.decUpvotes(event.messageId)
                }
                //Thumbs down
                else if (emoji == EmojiCodes.ThumbsDown) {
                    memeService.decDownvotes(event.messageId)
                }
            }
        }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {

        val guild = guildService.getGuild(event.guild.id) ?: return

        if (guild.memeChannelId == event.channel.id) {
            memeService.deleteMeme(guild.id, event.messageId)
        }
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {

        val author = event.author
        val message = event.message
        val guild = event.guild
        val member = guild.getMember(author)

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

                if (event.channel.id == guildService.getMemeChannel(event.guild.id)) {

                    if (event.message.attachments.isNotEmpty()) {
                        event.message.addReaction(EmojiCodes.ThumbsUp).queue()
                        event.message.addReaction(EmojiCodes.ThumbsDown).queue()
                        memeService.saveMeme(Meme(event.messageId, event.guild.id,
                                event.author.id, 0, 0, DateTime.now(), event.message.attachments[0].url))
                    }
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