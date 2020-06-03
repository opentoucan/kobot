package uk.me.danielharman.kotlinspringbot.listeners

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import uk.me.danielharman.kotlinspringbot.ApplicationLogger.logger
import uk.me.danielharman.kotlinspringbot.command.CommandFactory
import uk.me.danielharman.kotlinspringbot.provider.GuildMusicPlayerProvider
import uk.me.danielharman.kotlinspringbot.services.AttachmentService
import uk.me.danielharman.kotlinspringbot.services.GuildService
import uk.me.danielharman.kotlinspringbot.services.RequestService

class MessageListener(private val guildService: GuildService,
                      private val commandPrefix: String,
                      privilegedCommandPrefix: String,
                      featureRequestService: RequestService, attachmentService: AttachmentService) : ListenerAdapter() {

    private val playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    private val guildMusicPlayerProvider: GuildMusicPlayerProvider = GuildMusicPlayerProvider()
    private val commandFactory: CommandFactory = CommandFactory(
            guildService,
            featureRequestService,
            guildMusicPlayerProvider,
            commandPrefix,
            privilegedCommandPrefix,
            attachmentService)

    init {
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {

        val author = event.author
        val message = event.message
        val guild = event.guild
        val member = guild.getMember(author)

        logger.debug("[${guild.name}] #${event.channel.name} <${member?.nickname ?: author.asTag}>: ${message.contentDisplay}")

        if (author.isBot)
            return

        if (event.message.isMentioned(event.jda.selfUser, Message.MentionType.USER)) {
            val emotesByName = guild.getEmotesByName("piing", true)
            if (emotesByName.size >= 1)
                message.addReaction(emotesByName[0]).queue()
            else
                message.addReaction("U+1F621").queue()
        }

        when {
            message.contentStripped.startsWith(commandPrefix) -> {
                runCommand(event)
            }
            else -> {
                val words = message.contentStripped
                        .toLowerCase()
                        .replace(Regex("[.!?,$\\\\-]"), "")
                        .split(" ")
                        .filter { s -> s.isNotBlank() }

                if (words.size == 1 && words[0] == "lol") {
                    event.message.addReaction("U+1F923").queue()
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

        val cmd = event.message.contentStripped.split(" ")[0].removePrefix(commandPrefix)
        val command = commandFactory.getCommand(cmd)
        command.execute(event)
    }
}