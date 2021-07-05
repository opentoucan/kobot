package uk.me.danielharman.kotlinspringbot.command

import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.KotlinBotProperties
import uk.me.danielharman.kotlinspringbot.command.interfaces.Command
import uk.me.danielharman.kotlinspringbot.command.interfaces.ISlashCommand
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.events.DiscordMessageEvent

@Component
class ListCommandsCommand(private val commands: List<Command>, private val properties: KotlinBotProperties) :
    Command("help", "Get the list of inbuilt commands"), ISlashCommand {

    override fun execute(event: DiscordMessageEvent) {
        val stringBuilder = StringBuilder()
        commands.sortedBy { c -> c.commandString }.forEach { c ->
            run {
                stringBuilder.append("${properties.commandPrefix}${c.commandString}: ${c.description}\n")
            }
        }

        event.reply(
            Embeds.infoEmbedBuilder()
                .appendDescription("Text commands: ${properties.commandPrefix}help\n Voice commands: ${properties.voiceCommandPrefix}help\n\n\n")
                .appendDescription(stringBuilder.toString())
                .setTitle("Text Commands").build()
        )
    }

}