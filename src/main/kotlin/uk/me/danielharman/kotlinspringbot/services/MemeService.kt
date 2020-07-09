package uk.me.danielharman.kotlinspringbot.services

import org.joda.time.DateTime
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.ApplicationLogger.logger
import uk.me.danielharman.kotlinspringbot.models.Meme
import uk.me.danielharman.kotlinspringbot.repositories.MemeRepository

@Service
class MemeService(val mongoTemplate: MongoTemplate, val memeRepository: MemeRepository, val guildService: GuildService) {

    fun saveMeme(meme: Meme): Meme {
        logger.info("Saving meme $meme")
        return memeRepository.save(meme)
    }

    fun getMonthsMemes(guildId: String): List<Meme> {

        val now = DateTime.now()

        val lte = Criteria("created").lte(now.toDate())
        val gte = lte.gte(DateTime(now.year, now.monthOfYear, 1, 0, 0))

        val where = where("guildId").`is`(guildId)

        val query = Query().addCriteria(where).addCriteria(gte)

        return mongoTemplate.find(query, Meme::class.java)
    }

    fun decUpvotes(messageId: String) {

        val inc = Update().inc("upvotes", -1)

        mongoTemplate.findAndModify(Query(where("postId").`is`(messageId)), inc, Meme::class.java )
    }

    fun decDownvotes(messageId: String) {

        val inc = Update().inc("downvotes", -1)

        mongoTemplate.findAndModify(Query(where("postId").`is`(messageId)), inc, Meme::class.java )
    }

    fun incUpvotes(messageId: String) {

        val inc = Update().inc("upvotes", 1)

        mongoTemplate.findAndModify(Query(where("postId").`is`(messageId)), inc, Meme::class.java )
    }

    fun incDownvotes(messageId: String) {

        val inc = Update().inc("downvotes", 1)

        mongoTemplate.findAndModify(Query(where("postId").`is`(messageId)), inc, Meme::class.java )
    }


}