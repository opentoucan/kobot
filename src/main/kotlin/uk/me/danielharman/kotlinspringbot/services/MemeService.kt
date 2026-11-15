package uk.me.danielharman.kotlinspringbot.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.helpers.flatMap
import uk.me.danielharman.kotlinspringbot.models.Meme
import uk.me.danielharman.kotlinspringbot.repositories.MemeRepository
import java.time.LocalDateTime
import java.util.stream.Collectors

private const val MAX_MEME_LIST_SIZE = 10
private const val TOP_MEMES = 3
private const val THE_NUMBER_OF_DAYS_IN_A_WEEK_YOU_STUPID_LINTER = 7L

@Service
class MemeService(
    private val mongoTemplate: MongoOperations,
    private val memeRepository: MemeRepository,
    private val springGuildService: SpringGuildService,
) {
    enum class MemeInterval {
        WEEK,
        MONTH,
    }

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun saveMeme(meme: Meme): OperationResult<Meme, String> {
        logger.info("Saving meme")
        return Success(memeRepository.save(meme))
    }

    fun deleteMeme(
        guildId: String,
        messageId: String,
    ): OperationResult<Unit, String> {
        logger.info("Deleting meme")
        memeRepository.deleteByGuildIdAndMessageId(guildId, messageId)
        return Success(Unit)
    }

    fun getMeme(
        guildId: String,
        messageId: String,
    ): OperationResult<Meme, String> {
        val meme = memeRepository.findByGuildIdAndMessageId(guildId, messageId)
            ?: return Failure("Meme $messageId was not found in guild $guildId")
        return Success(meme)
    }

    data class MemeRanking(
        val userId: String,
        var upvotes: Int,
        var downvotes: Int,
        var count: Int,
    ) {
        val score: Int
            get() = upvotes - downvotes
    }

    fun getMemerIds(
        guildId: String,
        asc: Boolean = false,
    ): OperationResult<List<Pair<String, MemeRanking>>, String> = springGuildService.getGuild(guildId).flatMap {
        val idMap = HashMap<String, MemeRanking>()

        mongoTemplate.find(Query(where("guildId").`is`(guildId)), Meme::class.java).forEach { meme ->
            if (idMap.containsKey(meme.userId)) {
                idMap[meme.userId]!!.upvotes += meme.upvotes
                idMap[meme.userId]!!.downvotes += meme.downvotes
                idMap[meme.userId]!!.count++
            } else {
                idMap[meme.userId] = MemeRanking(meme.userId, meme.upvotes, meme.downvotes, 1)
            }
        }

        val rankings = if (asc) {
            idMap.toList()
                .sortedWith(compareBy<Pair<String, MemeRanking>> { (_, value) -> value.score }.thenBy { it.first })
                .take(MAX_MEME_LIST_SIZE)
        } else {
            idMap
                .toList()
                .sortedWith(compareByDescending<Pair<String, MemeRanking>> { (_, value) -> value.score }.thenBy { it.first })
                .filter { (_, value) -> value.score > 0 }.take(MAX_MEME_LIST_SIZE)
        }
        Success(rankings)
    }

    fun getTop3ByInterval(
        guildId: String,
        interval: MemeInterval,
    ): OperationResult<List<Meme>, String> {
        val now = LocalDateTime.now()

        val lte: Criteria
        val gte: Criteria
        when (interval) {
            MemeInterval.WEEK -> {
                lte = Criteria("created").lte(now)
                gte = lte.gte(LocalDateTime.now().minusDays(THE_NUMBER_OF_DAYS_IN_A_WEEK_YOU_STUPID_LINTER))
            }

            MemeInterval.MONTH -> {
                lte = Criteria("created").lte(now)
                gte = lte.gte(LocalDateTime.now().minusMonths(1))
            }
        }

        val where = where("guildId").`is`(guildId)

        val query = Query().addCriteria(where).addCriteria(gte)

        var memes = mongoTemplate.find(query, Meme::class.java)

        memes =
            memes
                .stream()
                .filter { m -> !(m.downvotes == 0 && m.upvotes == 0) }
                .sorted { o1, o2 -> o2.score - o1.score }
                .collect(Collectors.toList())

        return Success(memes.take(TOP_MEMES))
    }

    fun addDownvote(
        guildId: String,
        messageId: String,
        userId: String,
    ): OperationResult<Unit, String> = updateVote(guildId, messageId, Update().addToSet("downvoters", userId))

    fun removeUpvote(
        guildId: String,
        messageId: String,
        userId: String,
    ): OperationResult<Unit, String> = updateVote(guildId, messageId, Update().pull("upvoters", userId))

    fun addUpvote(
        guildId: String,
        messageId: String,
        userId: String,
    ): OperationResult<Unit, String> = updateVote(guildId, messageId, Update().addToSet("upvoters", userId))

    fun removeDownvote(
        guildId: String,
        messageId: String,
        userId: String,
    ): OperationResult<Unit, String> = updateVote(guildId, messageId, Update().pull("downvoters", userId))

    private fun updateVote(
        guildId: String,
        messageId: String,
        update: Update,
    ): OperationResult<Unit, String> = getMeme(guildId, messageId).flatMap { meme ->
        if (mongoTemplate.findAndModify(Query(where("id").`is`(meme.id)), update, Meme::class.java) == null) {
            Failure("Could not update meme $messageId in guild $guildId")
        } else {
            Success(Unit)
        }
    }
}
