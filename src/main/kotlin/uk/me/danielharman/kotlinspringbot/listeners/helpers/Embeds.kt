package uk.me.danielharman.kotlinspringbot.listeners.helpers

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

object Embeds {

    fun createHelpEmbed(commandPrefix: String): MessageEmbed = EmbedBuilder()
            .setColor(Color.green)
            .setTitle("Commands")
            .addField("Commands", "ping, userstats, info, save, " +
                    "play, skip, nowplaying, trackinfo, vol, volume, saved, help", false)
            .addField("Further help", "${commandPrefix}help <command>", true)
            .build()


    fun createErrorEmbed(message: String): MessageEmbed = EmbedBuilder()
            .setTitle("Error")
            .setDescription(message)
            .setColor(Color.RED)
            .build()

    fun infoEmbedBuilder(title: String = "Info", colour: Color = Color.blue) = EmbedBuilder()
            .setTitle(title)
            .setColor(colour)

    fun createInfoEmbed(): MessageEmbed =
            infoEmbedBuilder(title = "KotBot")
                    .appendDescription("This is a Discord bot written in Kotlin using Spring and Akka Actors")
                    .addField("Chumps", "Daniel Harman\nKieran Dennis", false)
                    .addField("Libraries", "https://akka.io, https://spring.io, https://kotlinlang.org", false)
                    .addField("Source", "https://gitlab.com/update-gitlab.yml/kotlinspringbot", false)
                    .build()

}