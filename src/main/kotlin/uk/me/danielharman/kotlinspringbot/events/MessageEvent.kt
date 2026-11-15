package uk.me.danielharman.kotlinspringbot.events

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.me.danielharman.kotlinspringbot.models.CommandParameter
import uk.me.danielharman.kotlinspringbot.models.ParsedCommandParameter

abstract class MessageEvent(
    val content: String,
    val origin: OriginService,
) {
    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)

    enum class OriginService {
        Discord,
    }

    abstract fun reply(
        msg: String,
        invokerOnly: Boolean = false,
    )

    abstract fun getParamValue(commandParameter: CommandParameter): ParsedCommandParameter

    fun parseParams(commandParameters: List<CommandParameter>): List<ParsedCommandParameter> {
        val parsed = mutableListOf<ParsedCommandParameter>()
        for (param in commandParameters) {
            parsed.add(getParamValue(param))
        }
        return parsed
    }
}
