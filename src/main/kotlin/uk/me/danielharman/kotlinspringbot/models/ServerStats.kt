package uk.me.danielharman.kotlinspringbot.models

import org.springframework.data.annotation.Id

class ServerStats(val serverId: String) {

    @Id
    private lateinit var id: String

    var wordCounts: HashMap<String, Int> = hashMapOf()
    var commandCounts: HashMap<String, Int> = hashMapOf()

    override fun toString(): String {
        return "ChannelStats(serverId='$serverId', id='$id', wordCounts=$wordCounts, commandCounts=$commandCounts)"
    }
}