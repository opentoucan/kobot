package uk.me.danielharman.kotlinspringbot.helpers

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

object Embeds {

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

}