package uk.me.danielharman.kotlinspringbot.command.voice

import net.dv8tion.jda.api.entities.VoiceChannel
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.audio.NewAudioResultHandler
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import uk.me.danielharman.kotlinspringbot.models.CommandParameter.ParamType
import uk.me.danielharman.kotlinspringbot.provider.GuildMusicPlayerProvider
import uk.me.danielharman.kotlinspringbot.services.DiscordActionService
import uk.me.danielharman.kotlinspringbot.services.DiscordCommandService
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

@Component
class PlayMusicCommand(
    private val guildMusicPlayerProvider: GuildMusicPlayerProvider,
    private val springGuildService: SpringGuildService,
    private val discordActionService: DiscordActionService,
    private val discordCommandService: DiscordCommandService,
    private val kotlinBotProperties: KotlinBotProperties
) : Command(
    "play", "Play audio via Youtube, Vimeo etc.",
    listOf(CommandParameter(0, "url", ParamType.Word, "Url to play music from"))
), ISlashCommand {

    override fun execute(event: DiscordMessageEvent) {
        val voiceChannel: VoiceChannel?

        val guild = event.guild

        if (guild == null) {
            event.reply(Embeds.createErrorEmbed("This command can only be used in Servers"))
            return
        }

        val paramValue = event.getParamValue(this.commandParameters[0])
        var url = paramValue.asString()

        if (url == null || paramValue.error) {
            val player = guildMusicPlayerProvider.getGuildAudioPlayer(guild).player
            player.isPaused = !player.isPaused

            val message = if (player.isPaused) "Paused" else "Playing"
            event.reply(message)
            return
        }

        if (url.startsWith(kotlinBotProperties.commandPrefix)){
           url = when(val command = discordCommandService.getCommand(guild.id, url.drop(1))){
               is Failure -> url
               is Success -> command.value.content?.trim()
           }
        }

        val member = guild.retrieveMember(event.author).complete()

        if (member == null) {
            event.reply("Can't find member!!!!")
            return
        }

        val voiceState = member.voiceState

        if (voiceState == null) {
            event.reply("Can't find member voicestate! Are you in a channel?")
            return
        }

        voiceChannel = voiceState.channel

        if (voiceChannel == null) {
            event.reply("Can't find voice channel! Are you in a channel?")
            return
        }
        when (val botVoiceChannel = discordActionService.getBotVoiceChannel(guild.id)) {
            is Success -> {
                logger.info("Bot's voice channel: " + botVoiceChannel.value.id)
                guild.audioManager.openAudioConnection(botVoiceChannel.value)
            }
        }

        if (voiceChannel != guild.audioManager.connectedChannel) {
            logger.info("My voice channel: " + voiceChannel.id)
            guild.audioManager.closeAudioConnection()
            guild.audioManager.openAudioConnection(voiceChannel)
        }

        logger.info("Connected voice channel from manager: " + guild.audioManager.connectedChannel?.id)
        val musicManager = guildMusicPlayerProvider.getGuildAudioPlayer(voiceChannel.guild)
        guildMusicPlayerProvider.playerManager.loadItemOrdered(
            musicManager,
            url,
            NewAudioResultHandler(voiceChannel, musicManager, event, springGuildService, guild)
        )
    }
}