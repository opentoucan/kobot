package uk.me.danielharman.kotlinspringbot.listeners

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.managers.AudioManager
import uk.me.danielharman.kotlinspringbot.ApplicationLogger.logger
import uk.me.danielharman.kotlinspringbot.audio.GuildMusicManager
import uk.me.danielharman.kotlinspringbot.audio.NewAudioResultHandler
import uk.me.danielharman.kotlinspringbot.services.GuildService
import kotlin.system.exitProcess

class MessageListener(private val guildService: GuildService, private val commandPrefix: String,
                      private val privilegedCommandPrefix: String, private val primaryPrivilegedUserId: String) : ListenerAdapter() {

    private val playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    private val musicManagers: HashMap<Long, GuildMusicManager> = hashMapOf()

    init {
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)
    }


    //region text

    override fun onGuildMessageReceived(message: GuildMessageReceivedEvent) {

        val author = message.author
        val message1 = message.message

        logger.info("[${message.guild.name}] #${message.channel.name} <${author.asTag}>: ${message1.contentStripped}")

        if (message1.contentStripped.startsWith(commandPrefix)) {
            runCommand(message)
        }
        else if (message1.contentStripped.startsWith(privilegedCommandPrefix)) {
            runPrivilegedCommand(message)
        }
        else {
            val words = message1.contentStripped
                    .toLowerCase()
                    .replace(Regex("[.!?,$\\\\-]"), "")
                    .split(" ")
                    .filter { s -> s.isNotBlank() }

            guildService.updateUserCount(message.guild.id, message.author.id, words.size)
            guildService.addWord(message.guild.id, words)

        }
    }

    //A lot of the var passing needs to be reworked to be more consistant
    private fun runCommand(message: GuildMessageReceivedEvent) {

        if(message.author.id == message.jda.selfUser.id || message.author.isBot)
        {
            logger.info("Not running command as author is me or a bot")
            return
        }

        val cmd = message.message.contentStripped.split(" ")[0].removePrefix(commandPrefix)
        val channel = message.channel

        when (cmd) {
            "ping" -> channel.sendMessage("pong").queue()
            "stats" -> channel.sendMessage(createStatsEmbed(message.guild.id, message)).queue()
            "userStats" -> channel.sendMessage(createUserWordCountsEmbed(message.guild.id, message)).queue()
            "info" -> channel.sendMessage(EmbedBuilder().setTitle("Kotlin Discord Bot")
                    .appendDescription("This is a Discord bot written in Kotlin using Spring and Akka Actors").build()).queue()
            "save" -> savePhrase(message)
            "play" -> loadAndPlay(message, channel, message.message.contentStripped.split(" ")[1])
            "skip" -> skipTrack(message.channel)

            //TODO add parse check
            "vol" -> vol(message.channel, message.message.contentStripped.split(" ")[1].toInt())
            else -> {
                channel.sendMessage(guildService.getCommand(message.guild.id, cmd)).queue()
            }
        }

    }

    private fun runPrivilegedCommand(message: GuildMessageReceivedEvent) {
        if(message.author.id == message.jda.selfUser.id || message.author.isBot)
        {
            logger.info("Not running command as author is me or a bot")
            return
        }

        val cmd = message.message.contentStripped.split(" ")[0].removePrefix(privilegedCommandPrefix)
        val channel = message.channel

        logger.info(cmd)

        if(message.author.id != primaryPrivilegedUserId
                && guildService.isPrivileged(message.guild.id, message.author.id)){
            channel.sendMessage("You are not a privileged user!").queue()
        }

        when (cmd) {
            "ping" -> channel.sendMessage("pong").queue()
            "addprivileged" -> guildService.addPrivileged(message.guild.id, message.message.contentStripped.split(" ")[1])
            "removeprivileged" -> guildService.removedPrivileged(message.guild.id, message.message.contentStripped.split(" ")[1])
            "privilegedusers" -> channel.sendMessage(createPrivilegedEmbed(message.guild.id, message)).queue()
            "disconnect" -> disconnect(message)
            "quit" -> {
                channel.sendMessage("Bye!").complete()
                exitProcess(0)
            }
            else -> channel.sendMessage("No such command $cmd").queue()
        }
    }

    private fun createPrivilegedEmbed(guildId: String, message: GuildMessageReceivedEvent): MessageEmbed {

        val guildName = message.guild.name

        val guild = guildService.getGuild(guildId)

        return if (guild == null) {
            EmbedBuilder().addField("error", "Could not find guild", false).build()
        } else {

            val stringBuilder = StringBuilder()

            val userById = message.jda.getUserById(primaryPrivilegedUserId)
            if (userById != null) {
                stringBuilder.append("Bot controller:  ${userById.name} - <$primaryPrivilegedUserId>\n")
            }

            guild.privilegedUsers.forEach { s ->
                run {
                    val name = message.jda.getUserById(s)
                    if (name != null) {
                        stringBuilder.append("${name.name} - <$s>\n")
                    }
                }
            }

            EmbedBuilder().appendDescription(stringBuilder.toString()).setColor(0x9d03fc).setTitle("Privileged users for $guildName").build()
        }

    }

    private fun createUserWordCountsEmbed(guildId: String, message: GuildMessageReceivedEvent): MessageEmbed {

        val guildName = message.guild.name

        val guild = guildService.getGuild(guildId)

        return if (guild == null) {
            EmbedBuilder().addField("error", "Could not find stats for server", false).build()
        } else {

            val comparator = Comparator { entry1: MutableMap.MutableEntry<String, Int>, entry2: MutableMap.MutableEntry<String, Int>
                ->
                entry2.value - entry1.value
            }

            val stringBuilder = StringBuilder()

            guild.userWordCounts.entries.stream().sorted(comparator).limit(20).forEach { (s, i) ->
                run {
                    val userById = message.jda.getUserById(s)
                    if (userById != null) {
                        stringBuilder.append("${userById.name} - $i\n")
                    }
                }
            }

            EmbedBuilder().appendDescription(stringBuilder.toString()).setColor(0x9d03fc).setTitle("Word said per user for $guildName").build()
        }

    }

    private fun savePhrase(message: GuildMessageReceivedEvent) {
        val content = message.message.contentStripped

        val split = content.split(" ")

        if (split.size < 3) {
            message.channel.sendMessage("Phrase missing").queue()
            return
        }

        guildService.saveCommand(message.guild.id, split[1], split.subList(2, split.size).joinToString(" "))

        message.channel.sendMessage("Saved!").queue()
    }

    private fun createStatsEmbed(guildId: String, message: GuildMessageReceivedEvent): MessageEmbed {

        val guildName = message.guild.name

        val stats = guildService.getGuild(guildId)

        return if (stats == null) {
            EmbedBuilder().addField("error", "Could not find stats for server", false).build()
        } else {

            val comparator = Comparator { entry1: MutableMap.MutableEntry<String, Int>, entry2: MutableMap.MutableEntry<String, Int>
                ->
                entry2.value - entry1.value
            }

            val stringBuilder = StringBuilder()

            stats.wordCounts.entries.stream().sorted(comparator).limit(20).forEach { (s, i) -> stringBuilder.append("$s - $i\n") }

            EmbedBuilder().appendDescription(stringBuilder.toString()).setColor(0x9d03fc).setTitle("Word counts for $guildName").build()
        }

    }
    //endregion


    //region Audio

    @Synchronized
    private fun getGuildAudioPlayer(guild: Guild): GuildMusicManager {
        val guildId = guild.idLong
        var musicManager = musicManagers.get(guildId)

        if (musicManager == null) {
            musicManager = GuildMusicManager(playerManager)
            musicManagers[guildId] = musicManager
        }

        guild.audioManager.sendingHandler = musicManager.getSendHandler()
        return musicManager
    }

    private fun loadAndPlay(message: GuildMessageReceivedEvent, channel: TextChannel, trackUrl: String) {
        val musicManager = getGuildAudioPlayer(channel.guild)

        val member = message.member

        if (member == null) {
            channel.sendMessage("Can't find member!!!!").queue()
            return
        }

        val voiceState = member.voiceState

        if (voiceState == null) {
            channel.sendMessage("Can't find member voicestate! Are you in a channel?").queue()
        }

        val voiceChannel = voiceState?.channel

        if (voiceChannel == null) {
            channel.sendMessage("Can't find voice channel! Are you in a channel?").queue()
        }

        playerManager.loadItemOrdered(musicManager, trackUrl, NewAudioResultHandler(voiceChannel, musicManager, channel, this))
        channel.sendMessage("Queuing $trackUrl").queue()
    }

    fun play(voiceChannel: VoiceChannel?, guild: Guild, musicManager: GuildMusicManager, track: AudioTrack) {
        joinUserVoiceChannel(voiceChannel, guild.audioManager)
        musicManager.scheduler.queue(track)
    }

    private fun skipTrack(channel: TextChannel) {
        val musicManager = getGuildAudioPlayer(channel.guild)
        musicManager.scheduler.nextTrack()
        channel.sendMessage("Skipped to next track.").queue()
    }

    private fun joinUserVoiceChannel(voiceChannel: VoiceChannel?, audioManager: AudioManager) {
        if (!audioManager.isConnected && !audioManager.isAttemptingToConnect) {
            try {
                audioManager.openAudioConnection(voiceChannel)
            } catch (e: InsufficientPermissionException) {
                logger.error("Bot encountered an exception when attempting to join a voice channel ${e.message}")
            }
        }
    }

    private fun vol(channel: TextChannel, vol: Int) {
        val musicManager = getGuildAudioPlayer(channel.guild)

        var newVol = vol

        if (newVol > 100) {
            newVol = 100
        } else if (newVol < 0) {
            newVol = 0
        }
        musicManager.player.volume = newVol
        channel.sendMessage("Setting volume to $newVol").queue()
    }

    private fun disconnect(message: GuildMessageReceivedEvent) {
        val audioManager = message.guild.audioManager
        if (audioManager.isConnected) audioManager.closeAudioConnection()
    }

    //endregion

}