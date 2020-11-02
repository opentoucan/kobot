package uk.me.danielharman.kotlinspringbot.models

import org.joda.time.DateTime
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "memes")
data class Meme(var messageId: String, var guildId: String, var userId: String, var url: String) {

    @Id
    lateinit var id: String

    var upvoters: List<String> = listOf()

    var downvoters: List<String> =  listOf()

    var created: DateTime =  DateTime.now()

    val score: Int
        get() = upvoters.size - downvoters.size

    val upvotes : Int
        get() = upvoters.size

    val downvotes: Int
        get() = downvoters.size

    override fun toString(): String {
        return "Meme(messageId='$messageId', guildId='$guildId', userId='$userId', upvoters=$upvoters, downvoters=$downvoters, url='$url', id='$id', created=$created)"
    }
}