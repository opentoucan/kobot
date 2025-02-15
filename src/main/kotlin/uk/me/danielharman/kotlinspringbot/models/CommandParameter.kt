package uk.me.danielharman.kotlinspringbot.models

import net.dv8tion.jda.api.entities.IMentionable

data class CommandParameter(
    val order: Int,
    val name: String,
    val type: ParamType,
    val description: String,
    val required: Boolean = false,
) {
    var value: Any? = null
    var error: Boolean = false

    enum class ParamType {
        Word,
        String,
        Long,
        Boolean,
        Mentionable,
    }

    fun reset() {
        value = null
        error = false
    }

    fun asBoolean(): Boolean? {
        if (type == ParamType.Boolean && value != null) {
            return value as Boolean
        }
        return null
    }

    fun asString(): String? {
        if ((type == ParamType.String || type == ParamType.Word) && value != null) {
            return value as String
        }
        return null
    }

    fun asLong(): Long? {
        if (type == ParamType.Long && value != null) {
            return value as Long
        }
        return null
    }

    fun asMentionable(): String? {
        if (type == ParamType.Mentionable && value != null) {
            return if (value is IMentionable) {
                (value as IMentionable).id
            } else {
                value as String
            }
        }
        return null
    }
}
