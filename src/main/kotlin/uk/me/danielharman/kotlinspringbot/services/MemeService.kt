package uk.me.danielharman.kotlinspringbot.services

import org.joda.time.DateTime
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.ApplicationLogger.logger
import uk.me.danielharman.kotlinspringbot.models.Meme
import uk.me.danielharman.kotlinspringbot.repositories.MemeRepository
import java.util.stream.Collectors

@Service
class MemeService(private val mongoTemplate: MongoTemplate,
                  private val memeRepository: MemeRepository, private val guildService: GuildService) {

    fun saveMeme(meme: Meme): Meme {
        logger.debug("Saving meme")
        return memeRepository.save(meme)
    }

    enum class MemeInterval {
        WEEK,
        MONTH
    }

    //This could probably be implemented using a mongodb aggregation function
    fun getMemerIds(guildId: String, asc: Boolean = false): List<Pair<String, Int>> {
        guildService.getGuild(guildId) ?: return listOf()

        val idMap = HashMap<String, Int>()

        mongoTemplate.find(Query(where("guildId").`is`(guildId)), Meme::class.java).forEach { meme ->
            idMap[meme.userId] = (idMap[meme.userId]?.plus(meme.upvotes)?.minus(meme.downvotes)
                    ?: (meme.upvotes - meme.downvotes))
        }

        return if (asc){
            val filtered = idMap
                    .toList()
                    .sortedBy { (_, value) -> value }
            if(filtered.size > 4){
                filtered.subList(0, 4)
            }else{
                filtered
            }
        }else {
            val filtered = idMap
                    .toList()
                    .sortedByDescending { (_, value) -> value }
                    .filter { (_, value) -> value > 0 }
            if(filtered.size > 4){
                filtered.subList(0, 4)
            }else{
                filtered
            }
        }
    }

    fun getTop3ByInterval(guildId: String, interval: MemeInterval): List<Meme> {

        val now = DateTime.now()

        val lte: Criteria
        val gte: Criteria
        when (interval) {
            MemeInterval.WEEK -> {
                lte = Criteria("created").lte(now.toDate())
                gte = lte.gte(DateTime.now().minusDays(7))
            }
            MemeInterval.MONTH -> {
                lte = Criteria("created").lte(now.toDate())
                gte = lte.gte(DateTime.now().minusMonths(1))
            }
        }

        val where = where("guildId").`is`(guildId)

        val query = Query().addCriteria(where).addCriteria(gte)

        var memes = mongoTemplate.find(query, Meme::class.java)

        memes = memes.stream()
                .filter { m -> !(m.downvotes == 0 && m.upvotes == 0) }
                .sorted { o1, o2 -> o2.getScore() - o1.getScore() }
                .collect(Collectors.toList())

        if (memes.size <= 3)
            return memes

        return memes.subList(0, 3)
    }

    fun decUpvotes(messageId: String) {

        val inc = Update().inc("upvotes", -1)

        mongoTemplate.findAndModify(Query(where("postId").`is`(messageId)), inc, Meme::class.java)
    }

    fun decDownvotes(messageId: String) {

        val inc = Update().inc("downvotes", -1)

        mongoTemplate.findAndModify(Query(where("postId").`is`(messageId)), inc, Meme::class.java)
    }

    fun incUpvotes(messageId: String) {

        val inc = Update().inc("upvotes", 1)

        mongoTemplate.findAndModify(Query(where("postId").`is`(messageId)), inc, Meme::class.java)
    }

    fun incDownvotes(messageId: String) {

        val inc = Update().inc("downvotes", 1)

        mongoTemplate.findAndModify(Query(where("postId").`is`(messageId)), inc, Meme::class.java)
    }


}