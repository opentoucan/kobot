package uk.me.danielharman.kotlinspringbot.models

import org.joda.time.DateTime
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "DiscordCommands")
class DiscordCommand(val guildId: String, val key: String, val content: String?, val fileName: String?,
                     val type: CommandType, val creatorId: String, val created: DateTime = DateTime.now()) {

    enum class CommandType { STRING, FILE }

    @Id
    lateinit var id: String

    override fun toString(): String {
        return "DiscordCommand(key='$key', content=$content, fileName=$fileName, type=$type, creatorId='$creatorId', id='$id', created=$created)"
    }

}