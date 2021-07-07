package uk.me.danielharman.kotlinspringbot.events

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.me.danielharman.kotlinspringbot.models.CommandParameter

abstract class MessageEvent(
    val content: String,
    val origin: OriginService
) {

    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)

    enum class OriginService {
        Discord
    }

    abstract fun reply(msg: String, invokerOnly: Boolean = false)
    abstract fun getParamValue(commandParameter: CommandParameter): CommandParameter

    fun parseParams(commandParameters: List<CommandParameter>): List<CommandParameter> {
        val parsed = mutableListOf<CommandParameter>()
        for (param in commandParameters) {
            parsed.add(getParamValue(param))
        }
        return parsed
    }
}

