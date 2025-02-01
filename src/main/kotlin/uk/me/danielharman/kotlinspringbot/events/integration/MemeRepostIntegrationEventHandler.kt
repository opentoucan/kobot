package uk.me.danielharman.kotlinspringbot.events.integration

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.models.DiscordChannelAttachment
import uk.me.danielharman.kotlinspringbot.models.DiscordChannelMessage
import uk.me.danielharman.kotlinspringbot.services.DiscordService
import java.text.DecimalFormat
import java.util.*


@Service
class MemeRepostIntegrationEventHandler(
    private val discordService: DiscordService
) {

    @Bean
    fun queue(): Queue {
        return Queue("meme-repost-queue", false)
    }

    @Bean
    fun exchange(): TopicExchange {
        return TopicExchange("meme-repost-exchange")
    }

    @Bean
    fun binding(queue: Queue?, exchange: TopicExchange?): Binding {
        return BindingBuilder.bind(queue).to(exchange).with("meme-repost-exchange")
    }

    @RabbitListener(queues = ["meme-repost-queue"])
    fun handle(content: String) {
        @OptIn(ExperimentalSerializationApi::class)
        val format = Json {namingStrategy = JsonNamingStrategy.SnakeCase}
        val memeRepostEvent = format.decodeFromString<MemeRepostIntegrationEvent>(content)
        val bytes = Base64.getDecoder().decode(memeRepostEvent.replyImage)

        val repostLink = memeRepostEvent.links.sortedBy { x -> x.score }
        val scorePercentageFormat = DecimalFormat("##.##")

        val discordMessage = DiscordChannelMessage(
            "Thou hath reposted! \n ${repostLink.joinToString("\n"){"https://discord.com/channels/${it.guildId}/${it.channelId}/${it.messageId} score: ${scorePercentageFormat.format(it.score * 100)}%"}}",
            memeRepostEvent.guildId,
            memeRepostEvent.channelId,
            attachments = listOf(DiscordChannelAttachment("repost.png", bytes)))

        discordService.sendChannelMessage(discordMessage)
    }
}