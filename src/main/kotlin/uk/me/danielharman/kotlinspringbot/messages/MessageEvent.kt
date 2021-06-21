package uk.me.danielharman.kotlinspringbot.messages

import uk.me.danielharman.kotlinspringbot.command.interfaces.Param

abstract class MessageEvent(
    val content: String,
    val origin: OriginService
) {

    enum class OriginService {
        Discord
    }

    abstract fun reply(msg: String)
    abstract fun parseParam(param: Param): String

    fun parseParams(params: List<Param>): List<String> {
        val parsed = mutableListOf<String>()
        for (param in params) {
            parsed.add(parseParam(param))
        }
        return parsed
    }
}

