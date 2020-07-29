package uk.me.danielharman.kotlinspringbot.models

import org.joda.time.DateTime
import org.springframework.data.annotation.Id

data class SpringGuild(private val guildId: String) {

    enum class CommandType {
        STRING,
        FILE
    }

    data class CustomCommand(val value: String, val type: CommandType,
                             val creatorId: String, val created: DateTime)

    @Id
    lateinit var id: String

    var wordCounts: HashMap<String, Int> = hashMapOf()
    var commandCounts: HashMap<String, Int> = hashMapOf()
    var userWordCounts: HashMap<String, Int> = hashMapOf()
    var savedCommands: HashMap<String, String> = hashMapOf()
    var customCommands: HashMap<String, CustomCommand> = hashMapOf()
    var privilegedUsers : List<String> = listOf()
    var logChannelId: String = ""
    var memeChannelId: String = ""
    var xkcdChannelId: String = ""
    var volume = 50

    override fun toString(): String {
        return "ChannelStats(guildId='$guildId', id='$id', wordCounts=$wordCounts, commandCounts=$commandCounts)"
    }
}