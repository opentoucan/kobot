package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.joda.time.format.ISODateTimeFormat
import uk.me.danielharman.kotlinspringbot.helpers.Embeds
import uk.me.danielharman.kotlinspringbot.services.GuildService

class InfoCommand(private val guildService: GuildService) : Command {
    override fun execute(event: GuildMessageReceivedEvent) {
        val split = event.message.contentStripped.split(" ")

        if (split.size > 1){

            val command = guildService.getCommand(event.guild.id, split[1])

            if (command == null){
                event.channel.sendMessage("Command not found").queue()
                return
            }

            val creatorName : String
            creatorName = if(command.creatorId.isEmpty())
                "Unknown"
            else
                event.jda.getUserById(command.creatorId)?.asTag ?: "Unknown"

            event.channel.sendMessage(Embeds.infoEmbedBuilder(title = "Command: ${split[1]}")
                    .appendDescription(command.value)
                    .addField("Creator", creatorName, false)
                    .addField("Created", command.created.toString(ISODateTimeFormat.dateTimeNoMillis()), false)
                    .build()).queue()

        }
        else {

            event.channel.sendMessage(Embeds.infoEmbedBuilder(title = "KotBot")
                    .appendDescription("This is a Discord bot written in Kotlin using Spring and Akka Actors")
                    .addField("Developers", "Daniel Harman\nKieran Dennis\nJared Prest", false)
                    .addField("Libraries", "https://akka.io, https://spring.io, https://kotlinlang.org", false)
                    .addField("Source", "https://gitlab.com/update-gitlab.yml/kotlinspringbot", false)
                    .addField("Licence", "https://www.apache.org/licenses/LICENSE-2.0", false)
                    .build()).queue()
        }
    }
}