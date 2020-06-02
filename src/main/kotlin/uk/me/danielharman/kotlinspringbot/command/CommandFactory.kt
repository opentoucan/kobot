package uk.me.danielharman.kotlinspringbot.command

import uk.me.danielharman.kotlinspringbot.provider.GuildMusicPlayerProvider
import uk.me.danielharman.kotlinspringbot.services.AttachmentService
import uk.me.danielharman.kotlinspringbot.services.GuildService
import uk.me.danielharman.kotlinspringbot.services.RequestService

class CommandFactory(private val guildService: GuildService,
                     private val featureRequestService: RequestService,
                     private val guildMusicPlayerProvider: GuildMusicPlayerProvider,
                     private val commandPrefix: String,
                     private val privilegedCommandPrefix: String,
                     private val attachmentService: AttachmentService) {
    fun getCommand(command: String): Command {
        return when (command) {
            "ping" -> PingCommand()
            "userstats" -> UserStatsCommand(guildService)
            "info" -> InfoCommand()
            "save" -> SavePhraseCommand(guildService, attachmentService)
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
            "help" -> HelpCommand(commandPrefix)
            "clear", "cleanup", "cls" -> ClearBotMessagesCommand(commandPrefix, privilegedCommandPrefix)
            "voicemove" -> VoiceMoveCommand()
            "deletecommand" -> DeleteCommand(guildService, attachmentService)
            else -> DefaultCommand(guildService, attachmentService, command)
        }
    }
}