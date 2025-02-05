package uk.me.danielharman.kotlinspringbot.events.integration

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service

@Service
class MemePostedIntegrationEventPublisher(private val rabbitTemplate: RabbitTemplate) {

    val topicExchangeName: String = "meme-posted-exchange"

    @Bean
    fun getExchange(): TopicExchange {
        return TopicExchange(topicExchangeName)
    }

    fun publish(event: MemePostedIntegrationEvent) {

        val payload = Json.encodeToString(event)
        rabbitTemplate.convertAndSend(topicExchangeName, "meme-posted-queue", payload)
    }
}
