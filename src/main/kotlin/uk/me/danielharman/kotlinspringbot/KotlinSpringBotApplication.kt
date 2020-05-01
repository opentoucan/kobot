package uk.me.danielharman.kotlinspringbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KotlinSpringBotApplication

fun main(args: Array<String>) {
	runApplication<KotlinSpringBotApplication>(*args)
}
