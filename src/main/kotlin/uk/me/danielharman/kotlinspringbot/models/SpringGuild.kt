package uk.me.danielharman.kotlinspringbot.models

import org.joda.time.DateTime
import org.springframework.data.annotation.Id

data class SpringGuild(val guildId: String) {

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
    var memeChannels: List<String> = listOf()
    var xkcdChannelId: String = ""
    var volume = 50
    var deafenedChannels: List<String> = listOf()

    override fun toString(): String {
        return "ChannelStats(guildId='$guildId', id='$id', wordCounts=$wordCounts, commandCounts=$commandCounts)"
    }
}