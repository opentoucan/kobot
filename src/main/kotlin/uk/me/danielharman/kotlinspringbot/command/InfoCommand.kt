package uk.me.danielharman.kotlinspringbot.command

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import uk.me.danielharman.kotlinspringbot.helpers.Embeds

class InfoCommand : Command {
    override fun execute(event: GuildMessageReceivedEvent) {
        event.channel.sendMessage(Embeds.infoEmbedBuilder(title = "KotBot")
                .appendDescription("This is a Discord bot written in Kotlin using Spring and Akka Actors")
                .addField("Chumps", "Daniel Harman\nKieran Dennis", false)
                .addField("Libraries", "https://akka.io, https://spring.io, https://kotlinlang.org", false)
                .addField("Source", "https://gitlab.com/update-gitlab.yml/kotlinspringbot", false)
                .build()).queue()
    }
}