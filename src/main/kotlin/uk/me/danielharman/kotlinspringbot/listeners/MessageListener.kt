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
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder
import uk.me.danielharman.kotlinspringbot.ApplicationLogger.logger
import uk.me.danielharman.kotlinspringbot.audio.GuildMusicManager
import uk.me.danielharman.kotlinspringbot.audio.NewAudioResultHandler
import uk.me.danielharman.kotlinspringbot.helpers.Comparators.mapStrIntComparator
import uk.me.danielharman.kotlinspringbot.services.GuildService
import java.awt.Color
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
    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {

        val author = event.author
        val message = event.message
        val guild = event.guild
        val member = guild.getMember(author)

        logger.debug("[${guild.name}] #${event.channel.name} <${member?.nickname ?: author.asTag}>: ${message.contentDisplay}")

        when {
            message.contentStripped.startsWith(commandPrefix) -> {
                runCommand(event)
            }
            message.contentStripped.startsWith(privilegedCommandPrefix) -> {
                runPrivilegedCommand(event)
            }
            else -> {
                val words = message.contentStripped
                        .toLowerCase()
                        .replace(Regex("[.!?,$\\\\-]"), "")
                        .split(" ")
                        .filter { s -> s.isNotBlank() }

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
        val channel = event.channel

        when (cmd) {
            "ping" -> channel.sendMessage("pong ${event.author.asMention}").queue()
            "userstats" -> channel.sendMessage(createUserWordCountsEmbed(event)).queue()
            "info" -> channel.sendMessage(createInfoEmbed()).queue()
            "save" -> savePhrase(event)
            "play" -> playMusic(event)
            "pause" -> todoMessage(event)
            "skip" -> skipTrack(event.channel)
            "nowplaying", "trackinfo" -> channel.sendMessage(trackInfo(event.channel)).queue()
            "vol", "volume" -> setVol(event)
            "saved" -> channel.sendMessage(createSavedCommandsEmbed(event.guild.id)).queue()
            "help" -> channel.sendMessage(createHelpEmbed()).queue()
            else -> {
                channel.sendMessage(guildService.getCommand(event.guild.id, cmd)).queue()
            }
        }

    }

    private fun playMusic(event: GuildMessageReceivedEvent){
        val split = event.message.contentStripped.split(" ")
        if(split.size < 2)
            togglePaused(event)

        loadAndPlay(event, event.channel, split[1])
    }

    private fun togglePaused(event: GuildMessageReceivedEvent) {
        val player = getGuildAudioPlayer(event.guild).player
        player.isPaused = !player.isPaused

        todoMessage(event)
    }


    private fun createHelpEmbed(): MessageEmbed = EmbedBuilder()
            .setColor(Color.green)
            .setTitle("Commands")
            .addField("Commands", "ping, userstats, info, save, " +
                    "play, skip, nowplaying, trackinfo, vol, volume, saved, help", false)
            .addField("Further help", "${commandPrefix}help <command>", true)
            .build()

    private fun createSavedCommandsEmbed(guildId: String): MessageEmbed {

        val guild = guildService.getGuild(guildId) ?: return createErrorEmbed("Guild not found")

        val stringBuilder = StringBuilder()
        guild.savedCommands.entries.forEach { (s, _) -> stringBuilder.append("$s ") }

        return EmbedBuilder()
                .setTitle("Saved commands")
                .setColor(0x9d03fc)
                .setDescription(stringBuilder.toString())
                .build()
    }

    private fun createErrorEmbed(message: String): MessageEmbed = EmbedBuilder()
            .setTitle("Error")
            .setDescription(message)
            .setColor(Color.RED)
            .build()

    private fun infoEmbedBuilder(title: String = "Info", colour: Color = Color.blue) = EmbedBuilder()
            .setTitle(title)
            .setColor(colour)

    private fun createInfoEmbed(): MessageEmbed =
            infoEmbedBuilder(title="Kot Bot")
            .appendDescription("This is a Discord bot written in Kotlin using Spring and Akka Actors")
            .addField("Chumps", "Daniel Harman\nKieran Dennis", false)
            .addField("Libraries", "https://akka.io, https://spring.io, https://kotlinlang.org", false)
            .addField("Source", "https://gitlab.com/update-gitlab.yml/kotlinspringbot", false)
            .build()

    private fun setVol(message: GuildMessageReceivedEvent) = vol(message.channel, message.message.contentStripped.split(" ")[1].toInt())

    private fun todoMessage(message: GuildMessageReceivedEvent) = message.channel.sendMessage("Not implemented").queue()

    private fun runPrivilegedCommand(event: GuildMessageReceivedEvent) {
        if (event.author.id == event.jda.selfUser.id || event.author.isBot) {
            logger.info("Not running command as author is me or a bot")
            return
        }

        val cmd = event.message.contentStripped.split(" ")[0].removePrefix(privilegedCommandPrefix)
        val channel = event.channel

        if (event.author.id != primaryPrivilegedUserId
                && guildService.isPrivileged(event.guild.id, event.author.id)) {
            channel.sendMessage("You are not a privileged user!").queue()
        }

        when (cmd) {
            "ping" -> channel.sendMessage("pong").queue()
            "addprivileged" -> addPrivileged(event)
            "removeprivileged" -> removePrivileged(event)
            "privilegedusers" -> channel.sendMessage(createPrivilegedEmbed(event.guild.id, event)).queue()
            "disconnect" -> disconnect(event)
            else -> channel.sendMessage("No such command $cmd").queue()
        }
    }

    private fun addPrivileged(message: GuildMessageReceivedEvent) = guildService.addPrivileged(message.guild.id, message.message.contentStripped.split(" ")[1])

    private fun removePrivileged(message: GuildMessageReceivedEvent) = guildService.removedPrivileged(message.guild.id, message.message.contentStripped.split(" ")[1])

    private fun createPrivilegedEmbed(guildId: String, message: GuildMessageReceivedEvent): MessageEmbed {

        val guildName = message.guild.name
        val guild = guildService.getGuild(guildId)

        return if (guild == null) {
            createErrorEmbed("Could not find data for $guildName")
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

            EmbedBuilder()
                    .appendDescription(stringBuilder.toString())
                    .setColor(0x9d03fc)
                    .setTitle("Privileged users for $guildName")
                    .build()
        }

    }

    private fun createUserWordCountsEmbed(message: GuildMessageReceivedEvent): MessageEmbed {

        val guildId = message.guild.id
        val guildName = message.guild.name
        val springGuild = guildService.getGuild(guildId)

        return if (springGuild == null) {
            EmbedBuilder().addField("error", "Could not find stats for server", false).build()
        } else {

            val stringBuilder = StringBuilder()

            springGuild.userWordCounts.entries
                    .stream()
                    .sorted(mapStrIntComparator)
                    .limit(20)
                    .forEach { (s, i) ->
                        stringBuilder.append("${message.jda.getUserById(s)?.name ?: s} - $i words\n")
                    }

            EmbedBuilder()
                    .appendDescription(stringBuilder.toString())
                    .setColor(0x9d03fc)
                    .setTitle("Word said per user for $guildName")
                    .build()
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

    //endregion


    //region Audio
    private fun trackInfo(channel: TextChannel): MessageEmbed {
        val guildAudioPlayer = getGuildAudioPlayer(channel.guild)

        val audioTrack = guildAudioPlayer.player.playingTrack
                ?: return EmbedBuilder().setTitle("Error").setColor(Color.red)
                        .setDescription("Not playing anything").build()

        val trackDuration = Period(audioTrack.duration)
        val playedDuration = Period(audioTrack.position)

        val fmt = PeriodFormatterBuilder()
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendHours()
                .appendSeparator(":")
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendMinutes()
                .appendSeparator(":")
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendSeconds()
                .toFormatter()

        val durationStr = "${fmt.print(playedDuration)}/${fmt.print(trackDuration)}"

        return EmbedBuilder()
                .setTitle("Music")
                .setColor(0x2e298f)
                .addField("Track Title", audioTrack.info.title, false)
                .addField("Track Author", audioTrack.info.author, false)
                .addField("Track Length", durationStr, false)
                .build()
    }

    @Synchronized
    private fun getGuildAudioPlayer(guild: Guild): GuildMusicManager {
        val guildId = guild.idLong
        var musicManager = musicManagers[guildId]

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
            return
        }

        val voiceChannel = voiceState.channel

        if (voiceChannel == null) {
            channel.sendMessage("Can't find voice channel! Are you in a channel?").queue()
            return
        }

        playerManager.loadItemOrdered(musicManager, trackUrl, NewAudioResultHandler(voiceChannel, musicManager, channel, this))
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
        if (voiceChannel == null)
            return

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

        val newVol = when {
            vol > 100 -> 100
            vol < 0 -> 0
            else -> vol
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