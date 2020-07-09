package uk.me.danielharman.kotlinspringbot.listeners

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import uk.me.danielharman.kotlinspringbot.ApplicationLogger.logger
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.services.CommandService
import uk.me.danielharman.kotlinspringbot.services.AdminCommandService
import uk.me.danielharman.kotlinspringbot.services.GuildService


class MessageListener(private val guildService: GuildService,
                      private val adminCommandService: AdminCommandService,
                      private val commandService: CommandService,
                      private val properties: KotlinBotProperties,
                      playerManager: AudioPlayerManager = DefaultAudioPlayerManager()) : ListenerAdapter() {
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
            message.contentStripped.startsWith(properties.commandPrefix) -> {
                runCommand(event)
            }
            message.contentStripped.startsWith(properties.privilegedCommandPrefix) ->
            {
                runAdminCommand(event)
            }
            else -> {

                if(event.channel.id == guildService.getMemeChannel(event.guild.id)){

                    if (event.message.attachments.isNotEmpty())
                    {
                        event.message.addReaction("U+1F44D").queue()
                        event.message.addReaction("U+1F44E").queue()
                    }
                }

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