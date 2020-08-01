package uk.me.danielharman.kotlinspringbot;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("discord")
data class KotlinBotProperties(
        var commandPrefix: String,
        var privilegedCommandPrefix: String,
        var primaryPrivilegedUserId: String,
        var token: String
)