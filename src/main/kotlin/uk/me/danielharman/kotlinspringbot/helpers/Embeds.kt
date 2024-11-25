package uk.me.danielharman.kotlinspringbot.helpers

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import uk.me.danielharman.kotlinspringbot.models.XkcdComic
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

    fun infoWithDescriptionEmbedBuilder(title: String = "Info", message: String, colour: Color = Color.blue): MessageEmbed = EmbedBuilder()
                .setTitle(title)
                .setDescription(message)
                .setColor(colour)
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

    fun createXkcdComicEmbed(xkcdComic: XkcdComic, title: String = "XKCD: "): MessageEmbed = EmbedBuilder()
            .setImage(xkcdComic.img)
            .addField("Title", xkcdComic.title, true)
            .addField("Published", "${xkcdComic.day}/${xkcdComic.month}/${xkcdComic.year}", true)
            .addField("Alt Text", xkcdComic.alt, true)
            .setTitle("$title #${xkcdComic.num}")
            .setColor(0x9d03fc)
            .build()

}