package uk.me.danielharman.kotlinspringbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import uk.me.danielharman.kotlinspringbot.properties.FeatureProperties
import uk.me.danielharman.kotlinspringbot.properties.KotlinBotProperties

@SpringBootApplication
@EnableConfigurationProperties(KotlinBotProperties::class, FeatureProperties::class)
class KotlinSpringBotApplication

fun main(args: Array<String>) {
    @Suppress("SpreadOperator") 
    runApplication<KotlinSpringBotApplication>(*args)
}
