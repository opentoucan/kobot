package uk.me.danielharman.kotlinspringbot.command.voice

import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import uk.me.danielharman.kotlinspringbot.models.CommandParameter.ParamType
import uk.me.danielharman.kotlinspringbot.provider.GuildMusicPlayerProvider
import uk.me.danielharman.kotlinspringbot.services.SpringGuildService

@Component
class SetVolumeCommand(
    private val guildMusicPlayerProvider: GuildMusicPlayerProvider,
    private val springGuildService: SpringGuildService,
) : Command(
        "vol",
        "Set the bot's volume level (0-100)",
        listOf(
            CommandParameter(0, "volume", ParamType.Long, "Volume to set the bot to (0-100)", true),
        ),
    ),
    ISlashCommand {
    override fun execute(event: DiscordMessageEvent) {
        if (event.guild == null) {
            event.reply(Embeds.createErrorEmbed("This command can only be used in Servers"))
            return
        }

        val paramValue = event.getParamValue(commandParameters[0])
        val vol = paramValue.asLong()

        if (vol == null || paramValue.error) {
            event.reply(Embeds.createErrorEmbed("Invalid volume"))
            return
        }

        val musicManager = guildMusicPlayerProvider.getGuildAudioPlayer(event.guild)

        val newVol =
            when {
                vol > 100 -> 100
                vol < 0 -> 0
                else -> vol
            }
        musicManager.player.volume = newVol.toInt()
        springGuildService.setVol(event.guild.id, newVol.toInt())
        event.reply("Setting volume to $newVol")
    }
}
