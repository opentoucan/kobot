package uk.me.danielharman.kotlinspringbot.models

import org.joda.time.DateTime
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "DiscordCommands")
class DiscordCommand(private val guildId: String, private val key: String, private val content: String?, private val fileName: String?,
                     private val type: CommandType, private val creatorId: String, private val created: DateTime = DateTime.now()) {

    enum class CommandType { STRING, FILE }

    @Id
    private lateinit var id: String

    override fun toString(): String {
        return "DiscordCommand(key='$key', content=$content, fileName=$fileName, type=$type, creatorId='$creatorId', id='$id', created=$created)"
    }

}