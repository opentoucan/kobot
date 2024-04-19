package uk.me.danielharman.kotlinspringbot.command.voice

import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent

@Component
class PlayMusicCommand : Command(
    "play", "Play audio via Youtube, Vimeo etc."), ISlashCommand {
    override fun execute(event: DiscordMessageEvent) {
        event.reply("Playing music has been disabled. Please use alternative bot.")
    }
}