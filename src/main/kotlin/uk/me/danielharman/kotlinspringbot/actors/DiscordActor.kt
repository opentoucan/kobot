package uk.me.danielharman.kotlinspringbot.actors

import akka.actor.UntypedAbstractActor
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.ApplicationLogger.logger
import uk.me.danielharman.kotlinspringbot.conf.Settings.commandPrefix
import uk.me.danielharman.kotlinspringbot.services.GuildService
import kotlin.collections.MutableMap.MutableEntry

@Component
@Scope("prototype")
class DiscordActor(val guildService: GuildService) : UntypedAbstractActor() {

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
                .setActivity(Activity.of(Activity.ActivityType.DEFAULT, "Doing bot things"))
                .addEventListeners(MessageListener(guildService))

        jda = builder.build().awaitReady()

        logger.debug("End")
    }

}

class MessageListener(val guildService: GuildService) : ListenerAdapter() {

    override fun onGuildMessageReceived(message: GuildMessageReceivedEvent) {

        val author = message.author
        val message1 = message.message

        logger.info("${author.asTag} said ${message1.contentStripped}")

        if (message1.contentStripped.startsWith(commandPrefix)) {
            runCommand(message)
        } else {

            val words = message1.contentStripped
                    .toLowerCase()
                    .replace(Regex("[.!?,\\\\-]"), "")
                    .split(" ")
                    .filter { s -> s.isNotBlank() }

            guildService.updateUserCount(message.guild.id, message.author.id, words.size)
            guildService.addWord(message.guild.id, words)

        }
    }

    private fun runCommand(message: GuildMessageReceivedEvent) {
        val cmd = message.message.contentStripped.split(" ")[0].removePrefix(commandPrefix)
        val channel = message.channel

        when (cmd) {
            "ping" -> channel.sendMessage("pong").complete()
            "stats" -> channel.sendMessage(createStatsEmbed(message.guild.id, message)).complete()
            "userStats" -> channel.sendMessage(createUserWordCountsEmbed(message.guild.id, message)).complete()
            "info" -> channel.sendMessage(EmbedBuilder().setTitle("Kotlin Discord Bot").appendDescription("This is a Discord bot written in Kotlin using Spring and Akka Actors").build()).complete()
            "save" -> savePhrase(message)
            else -> {
                channel.sendMessage(guildService.getCommand(message.guild.id, cmd)).complete()
            }
        }

    }

    private fun createUserWordCountsEmbed(guildId: String, message: GuildMessageReceivedEvent): MessageEmbed {

        val guildName = message.guild.name

        val guild = guildService.getGuild(guildId)

        return if (guild == null) {
            EmbedBuilder().addField("error", "Could not find stats for server", false).build()
        } else {

            val comparator = Comparator { entry1: MutableEntry<String, Int>, entry2: MutableEntry<String, Int>
                ->
                entry2.value - entry1.value
            }

            val stringBuilder = StringBuilder()

            guild.userWordCounts.entries.stream().sorted(comparator).limit(20).forEach { (s, i) ->
                run {
                    val userById = message.jda.getUserById(s)
                    if (userById != null) {
                        stringBuilder.append("${userById.name} - $i\n")
                    }
                }
            }

            EmbedBuilder().appendDescription(stringBuilder.toString()).setColor(0x9d03fc).setTitle("Word said per user for $guildName").build()
        }

    }

    private fun savePhrase(message: GuildMessageReceivedEvent) {
        val content = message.message.contentStripped

        val split = content.split(" ")

        if (split.size < 3) {
            message.channel.sendMessage("Phrase missing").complete()
            return
        }

        guildService.saveCommand(message.guild.id, split[1], split.subList(2, split.size).joinToString(" "))

        message.channel.sendMessage("Saved!").complete()
    }

    private fun createStatsEmbed(guildId: String, message: GuildMessageReceivedEvent): MessageEmbed {

        val guildName = message.guild.name

        val stats = guildService.getGuild(guildId)

        return if (stats == null) {
            EmbedBuilder().addField("error", "Could not find stats for server", false).build()
        } else {

            val comparator = Comparator { entry1: MutableEntry<String, Int>, entry2: MutableEntry<String, Int>
                ->
                entry2.value - entry1.value
            }

            val stringBuilder = StringBuilder()

            stats.wordCounts.entries.stream().sorted(comparator).limit(20).forEach { (s, i) -> stringBuilder.append("$s - $i\n") }

            EmbedBuilder().appendDescription(stringBuilder.toString()).setColor(0x9d03fc).setTitle("Word counts for $guildName").build()
        }

    }

}