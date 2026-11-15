package uk.me.danielharman.kotlinspringbot.models

import net.dv8tion.jda.api.entities.IMentionable

data class CommandParameter(
    val order: Int,
    val name: String,
    val type: ParamType,
    val description: String,
    val required: Boolean = false,
) {
    enum class ParamType {
        Word,
        String,
        Long,
        Boolean,
        Mentionable,
    }
}

data class ParsedCommandParameter(
    val definition: CommandParameter,
    val value: Any?,
    val error: Boolean,
) {
    private val type: CommandParameter.ParamType
        get() = definition.type

    fun asBoolean(): Boolean? {
        if (type == CommandParameter.ParamType.Boolean && value != null) {
            return value as Boolean
        }
        return null
    }

    fun asString(): String? {
        if ((type == CommandParameter.ParamType.String || type == CommandParameter.ParamType.Word) && value != null) {
            return value as String
        }
        return null
    }

    fun asLong(): Long? {
        if (type == CommandParameter.ParamType.Long && value != null) {
            return value as Long
        }
        return null
    }

    fun asMentionable(): String? {
        if (type == CommandParameter.ParamType.Mentionable && value != null) {
            return if (value is IMentionable) {
                value.id
            } else {
                value as String
            }
        }
        return null
    }
}
