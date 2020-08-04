package uk.me.danielharman.kotlinspringbot.models

import org.joda.time.DateTime
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collation = "memes")
data class Meme(var postId: String, var guildId: String, var userId: String,
                var upvotes: Int, var downvotes: Int, var created: DateTime, var url: String) {

    @Id
    lateinit var id: String
    override fun toString(): String {
        return "Meme(postId='$postId', guildId='$guildId', userId='$userId', upvotes=$upvotes, downvotes=$downvotes, created=$created', url=$url)"
    }


}