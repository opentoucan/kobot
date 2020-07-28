package uk.me.danielharman.kotlinspringbot

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("xkcd")
data class XkcdProperties(
        var latestUrl: String)