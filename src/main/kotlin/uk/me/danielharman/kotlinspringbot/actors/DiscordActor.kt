package uk.me.danielharman.kotlinspringbot.actors

import akka.actor.UntypedAbstractActor
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.MessageCreateSpec
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import uk.me.danielharman.kotlinspringbot.ApplicationLogger.logger
import uk.me.danielharman.kotlinspringbot.services.StatsService
import java.util.function.Consumer

@Component
@Scope("prototype")
class DiscordActor(val statsService: StatsService) : UntypedAbstractActor() {

    fun start() {

        logger.info("Starting discord")

        DiscordClient.create("Mjc4OTEyMjQ4NzEyMjY1NzI5.XqxwDA.trilgNR2HmNDJRFo2OgtuSa33aA")
                .withGateway { client: GatewayDiscordClient ->
                    client.eventDispatcher.on(ReadyEvent::class.java)
                            .subscribe { ready: ReadyEvent -> println("Logged in as " + ready.self.username) }
                    client.eventDispatcher.on(MessageCreateEvent::class.java)
                            .map { obj: MessageCreateEvent -> handleMsg(obj) }
                            .subscribe()
                    client.onDisconnect()
                }.block()

        logger.debug("End")
    }

    private fun handleMsg(msg: MessageCreateEvent) {
        val user = msg.message.author.get();
        val content = msg.message.content
        val channel = msg.message.channel

        if (content.startsWith("!")) {
            logger.info("${user.tag} used command $content")
            handleCommands(msg)
        } else if (content.toLowerCase().contains("gay") && user.id.asString() == "267422367146442753") {
            channel.flatMap { obj: MessageChannel -> obj.createMessage("${user.mention} watch your language") }
                    .subscribe()
        } else {
            logger.info("${user.tag} said $content")
            statsService.addWord(msg.guildId.get().asString(), content.toLowerCase().replace(Regex("[.!?\\\\-]"), "").split(" "))
        }

    }

    private fun handleCommands(cmdMsg: MessageCreateEvent) {

        val content = cmdMsg.message.content.removePrefix("!")
        val channel = cmdMsg.message.channel;

        when (content) {
            "ping" -> sendMessage(channel, "ping")
            "stats" -> sendStats(cmdMsg)
        }

    }

    private fun sendStats(msg: MessageCreateEvent) {

        val stats = statsService.getStats(msg.guildId.get().asString())

        if (stats == null)
            logger.error("Stats for ${msg.guildId.get().asString()} were null")

        val stringBuilder = StringBuilder()

        val comparator = Comparator { entry1: MutableMap.MutableEntry<String, Int>, entry2: MutableMap.MutableEntry<String, Int>
            ->
            entry2.value - entry1.value
        }

        stats!!.wordCounts.entries.stream().sorted(comparator).limit(20).forEach { (s, i) -> stringBuilder.append("$s - $i\n") }

        val embedSpec: Consumer<EmbedCreateSpec> = Consumer { spec: EmbedCreateSpec ->
            {}
            spec.setTitle("Stats for ${msg.guildId.get().asString()} ")
            spec.setColor(0x9d03fc)
            spec.setDescription(stringBuilder.toString())
        }

        msg.message.channel.flatMap { obj: MessageChannel ->
            obj.createMessage { spec: MessageCreateSpec ->
                spec.setEmbed(embedSpec)
            }
        }.subscribe()
    }

    private fun sendMessage(channel: Mono<MessageChannel>, msg: String) {
        channel.flatMap { obj: MessageChannel -> obj.createMessage(msg) }.subscribe()
    }

    override fun onReceive(message: Any?) = when (message) {
        "start" -> start()
        else -> println("received unknown message")
    }

}