package uk.me.danielharman.kotlinspringbot.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "memes")
data class Meme(
    var messageId: String,
    var guildId: String,
    var userId: String,
    var url: String,
    var channelId: String,
    var urlType: UrlType = UrlType.Image,
) {
    enum class UrlType {
        Image,
        Link,
    }

    @Id lateinit var id: String

    var upvoters: List<String> = listOf()

    var downvoters: List<String> = listOf()

    var created: LocalDateTime = LocalDateTime.now()

    val score: Int
        get() = upvoters.size - downvoters.size

    val upvotes: Int
        get() = upvoters.size

    val downvotes: Int
        get() = downvoters.size

    override fun toString(): String = "Meme(messageId='$messageId', guildId='$guildId'," +
        " userId='$userId', upvoters=$upvoters, downvoters=$downvoters, url='$url', id='$id', created=$created)"
}
