package uk.me.danielharman.kotlinspringbot.command.interfaces

data class Param(
    val order: Int,
    val name: String,
    val type: ParamType,
    val description: String,
    val required: Boolean = false
) {
    enum class ParamType {
        Word,
        Text,
        Int,
        Boolean,
        Mentionable
    }
}