package uk.me.danielharman.kotlinspringbot.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("discord")
data class KotlinBotProperties(
    var commandPrefix: String,
    var voiceCommandPrefix: String,
    var privilegedCommandPrefix: String,
    var primaryPrivilegedUserId: String,
    var token: String,
)
