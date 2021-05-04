package uk.me.danielharman.kotlinspringbot.services

import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.models.Meme
import uk.me.danielharman.kotlinspringbot.repositories.MemeRepository
import java.util.stream.Collectors

@Service
class MemeService(private val mongoTemplate: MongoTemplate,
                  private val memeRepository: MemeRepository,
                  private val springGuildService: SpringGuildService) {

    enum class MemeInterval {
        WEEK,
        MONTH
    }

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun saveMeme(meme: Meme): Meme {
        logger.info("Saving meme")
        return memeRepository.save(meme)
    }

    fun deleteMeme(guildId: String, messageId: String) {
        logger.info("Deleting meme")
        memeRepository.deleteByGuildIdAndMessageId(guildId, messageId)
    }

    fun getMeme(guildId: String, messageId: String): Meme? = memeRepository.findByGuildIdAndMessageId(guildId, messageId)

    data class MemeRanking(val userId: String, var upvotes: Int, var downvotes: Int, var count: Int) {
        val score: Int
            get() = upvotes - downvotes
    }

    fun getMemerIds(guildId: String, asc: Boolean = false): List<Pair<String, MemeRanking>> {
        springGuildService.getGuild(guildId) ?: return listOf()

        val idMap = HashMap<String, MemeRanking>()

        mongoTemplate.find(Query(where("guildId").`is`(guildId)), Meme::class.java).forEach { meme ->
            run {

                if (idMap.containsKey(meme.userId)) {
                    idMap[meme.userId]!!.upvotes += meme.upvotes
                    idMap[meme.userId]!!.downvotes += meme.downvotes
                    idMap[meme.userId]!!.count++
                } else {
                    idMap[meme.userId] = MemeRanking(meme.userId, meme.upvotes, meme.downvotes, 1)
                }

            }

        }

        return if (asc) {
            val filtered = idMap
                    .toList()
                    .sortedBy { (_, value) -> value.score }
            if (filtered.size > 10) {
                filtered.subList(0, 10)
            } else {
                filtered
            }
        } else {
            val filtered = idMap
                    .toList()
                    .sortedByDescending { (_, value) -> value.score }
                    .filter { (_, value) -> value.score > 0 }
            if (filtered.size > 10) {
                filtered.subList(0, 10)
            } else {
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
                .sorted { o1, o2 -> o2.score - o1.score }
                .collect(Collectors.toList())

        if (memes.size <= 3)
            return memes

        return memes.subList(0, 3)
    }

    fun addDownvote(guildId: String, messageId: String, userId: String): Boolean {
        val meme = getMeme(guildId, messageId) ?: return false

        val push = Update().push("downvoters", userId)
        mongoTemplate.findAndModify(Query(where("id").`is`(meme.id)), push, Meme::class.java)
        return true
    }

    fun removeUpvote(guildId: String, messageId: String, userId: String): Boolean {
        val meme = getMeme(guildId, messageId) ?: return false

        val pull = Update().pull("upvoters", userId)
        mongoTemplate.findAndModify(Query(where("id").`is`(meme.id)), pull, Meme::class.java)
        return true
    }

    fun addUpvote(guildId: String, messageId: String, userId: String): Boolean {
        val meme = getMeme(guildId, messageId) ?: return false

        val push = Update().push("upvoters", userId)
        mongoTemplate.findAndModify(Query(where("id").`is`(meme.id)), push, Meme::class.java)
        return true
    }

    fun removeDownvote(guildId: String, messageId: String, userId: String): Boolean {
        val meme = getMeme(guildId, messageId) ?: return false

        val pull = Update().pull("downvoters", userId)
        mongoTemplate.findAndModify(Query(where("id").`is`(meme.id)), pull, Meme::class.java)
        return true
    }
}