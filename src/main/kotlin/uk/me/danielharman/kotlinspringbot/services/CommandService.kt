package uk.me.danielharman.kotlinspringbot.services

import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.command.*
import uk.me.danielharman.kotlinspringbot.provider.GuildMusicPlayerProvider

@Service
class CommandService(private val guildService: GuildService,
                     private val featureRequestService: RequestService,
                     private val guildMusicPlayerProvider: GuildMusicPlayerProvider,
                     private val attachmentService: AttachmentService,
                     private val properties: KotlinBotProperties) {

    fun getCommand(command: String): Command {
        return when (command) {
            "ping" -> PingCommand()
            "userstats" -> UserStatsCommand(guildService)
            "info" -> InfoCommand()
            "save", "set" -> SavePhraseCommand(guildService, attachmentService)
            "feature", "savefeature", "newfeature", "request" -> FeatureRequestCommand(featureRequestService)
            "features", "requests" -> ListFeaturesCommand(featureRequestService)
            "getfeature", "getrequest" -> FetchFeatureCommand(featureRequestService)
            "play" -> PlayMusicCommand(guildMusicPlayerProvider, guildService)
            "skip" -> SkipTrackCommand(guildMusicPlayerProvider)
            "avatar" -> ShowAvatarCommand()
            "nowplaying", "trackinfo", "playing" -> TrackInfoCommand(guildMusicPlayerProvider)
            "vol", "volume" -> SetVolumeCommand(guildMusicPlayerProvider, guildService)
            "getvol", "getvolume" -> GetVolumeCommand(guildService)
            "saved" -> FetchSavedCommand(guildService)
            "help" -> HelpCommand(properties.commandPrefix)
            "clear", "cleanup", "cls" -> ClearBotMessagesCommand(properties.commandPrefix, properties.privilegedCommandPrefix)
            "voicemove" -> VoiceMoveCommand()
            "deletecommand" -> DeleteCommand(guildService, attachmentService)
            "summon", "join", "connect" -> SummonCommand()
            "disconnect", "leave", "banish" -> DisconnectCommand()
            else -> CustomCommand(guildService, attachmentService, command)
        }
    }
}