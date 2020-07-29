package uk.me.danielharman.kotlinspringbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(KotlinBotProperties::class)
class KotlinSpringBotApplication

fun main(args: Array<String>) {
    runApplication<KotlinSpringBotApplication>(*args)
}
