package uk.me.danielharman.kotlinspringbot.command

import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.messages.DiscordMessageEvent

@Component
class PingCommand : Command("ping", "pong") {
    override fun execute(event: DiscordMessageEvent) = event.reply("pong ${event.author.asMention}")
}