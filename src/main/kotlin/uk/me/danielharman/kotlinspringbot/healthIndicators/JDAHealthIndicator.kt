package uk.me.danielharman.kotlinspringbot.healthIndicators

import net.dv8tion.jda.api.JDA.Status.CONNECTED
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.objects.DiscordObject


@Component
class JDAHealthIndicator : HealthIndicator {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun health(): Health {
        val status = DiscordObject.jda.status
        if (status == CONNECTED)
            return Health.up().build()
        logger.info("Health Probe failed, JDA status: $status")
        return Health.down().withDetail("jda-status", status).build()
    }
}