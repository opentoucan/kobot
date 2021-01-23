package uk.me.danielharman.kotlinspringbot.objects

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ApplicationLogger {
    @Deprecated("Use in class logger")
    val logger: Logger = LoggerFactory.getLogger("application")
}