package uk.me.danielharman.kotlinspringbot.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "features") data class FeatureProperties(val memeRepost: Boolean)
