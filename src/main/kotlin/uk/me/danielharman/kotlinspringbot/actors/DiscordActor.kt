package uk.me.danielharman.kotlinspringbot.actors

import akka.actor.UntypedAbstractActor
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.ApplicationLogger.logger
import uk.me.danielharman.kotlinspringbot.conf.Settings.commandPrefix
import uk.me.danielharman.kotlinspringbot.services.StatsService
import kotlin.collections.MutableMap.MutableEntry

@Component
@Scope("prototype")
class DiscordActor(val statsService: StatsService) : UntypedAbstractActor() {

    private lateinit var jda: JDA

    @Value("\${discord.token}")
    private lateinit var token: String

    override fun onReceive(message: Any?) = when (message) {
        "start" -> start()
        else -> println("received unknown message")
    }

    fun start() {

        logger.info("Starting discord")

        val builder: JDABuilder = JDABuilder.create(
                token,
                GUILD_MEMBERS,
                GUILD_PRESENCES,
                DIRECT_MESSAGES,
                GUILD_MESSAGES,
                GUILD_VOICE_STATES,
                GUILD_EMOJIS,
                GUILD_MESSAGE_REACTIONS)
                .addEventListeners(MessageListener(statsService))

        jda = builder.build().awaitReady()

        logger.debug("End")
    }

}

class MessageListener(val statsService: StatsService) : ListenerAdapter() {

    override fun onGuildMessageReceived(message: GuildMessageReceivedEvent) {

        val author = message.author
        val message1 = message.message

        logger.info("${author.asTag} said ${message1.contentStripped}")

        if (message1.contentStripped.startsWith(commandPrefix)) {
            runCommand(message)
        } else {
            statsService.addWord(message.guild.id, message1.contentStripped
                    .toLowerCase()
                    .replace(Regex("[.!?,\\\\-]"), "")
                    .split(" ")
                    .filter { s -> s.isNotBlank() }
            )
        }
    }

    private fun runCommand(message: GuildMessageReceivedEvent) {
        val cmd = message.message.contentStripped.split(" ")[0].removePrefix(commandPrefix)
        val channel = message.channel

        when (cmd) {
            "ping" -> channel.sendMessage("pong").complete()
            "stats" -> channel.sendMessage(createStatsEmbed(message.guild.id)).complete()
            "info" -> channel.sendMessage(EmbedBuilder().setTitle("Kotlin Discord Bot").appendDescription("This is a Discord bot written in Kotlin using Spring and Akka Actors").build()).complete()
            else -> channel.sendMessage("No such command $cmd").complete()
        }

    }

    private fun createStatsEmbed(serverId: String): MessageEmbed {

        val stats = statsService.getStats(serverId)

        return if (stats == null) {
            EmbedBuilder().addField("error", "Could not find stats for server", false).build()
        } else {

            val comparator = Comparator { entry1: MutableEntry<String, Int>, entry2: MutableEntry<String, Int>
                ->
                entry2.value - entry1.value
            }

            val stringBuilder = StringBuilder()

            stats.wordCounts.entries.stream().sorted(comparator).limit(20).forEach { (s, i) -> stringBuilder.append("$s - $i\n") }

            EmbedBuilder().appendDescription(stringBuilder.toString()).setColor(0x9d03fc).setTitle("Stats for $serverId").build()
        }

    }

}