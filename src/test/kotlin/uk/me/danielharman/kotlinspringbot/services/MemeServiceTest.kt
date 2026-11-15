package uk.me.danielharman.kotlinspringbot.services

import org.bson.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.helpers.assertFailure
import uk.me.danielharman.kotlinspringbot.helpers.assertSuccess
import uk.me.danielharman.kotlinspringbot.models.Meme
import uk.me.danielharman.kotlinspringbot.models.SpringGuild
import uk.me.danielharman.kotlinspringbot.repositories.MemeRepository

internal class MemeServiceTest {
    private val mongoTemplate: MongoOperations = mock(MongoOperations::class.java)
    private val memeRepository: MemeRepository = mock(MemeRepository::class.java)
    private val springGuildService: SpringGuildService = mock(SpringGuildService::class.java)
    private val service = MemeService(mongoTemplate, memeRepository, springGuildService)

    @Test
    fun `saveMeme returns saved meme`() {
        val meme = meme("message")
        `when`(memeRepository.save(meme)).thenReturn(meme)

        assertSame(meme, assertSuccess(service.saveMeme(meme)).value)

        verify(memeRepository).save(meme)
    }

    @Test
    fun `deleteMeme deletes meme by guild and message`() {
        assertSuccess(service.deleteMeme("guild", "message"))

        verify(memeRepository).deleteByGuildIdAndMessageId("guild", "message")
    }

    @Test
    fun `getMeme returns matching meme`() {
        val meme = meme("message")
        `when`(memeRepository.findByGuildIdAndMessageId("guild", "message")).thenReturn(meme)

        assertSame(meme, assertSuccess(service.getMeme("guild", "message")).value)
    }

    @Test
    fun `getMeme returns failure when no meme matches`() {
        `when`(memeRepository.findByGuildIdAndMessageId("guild", "missing")).thenReturn(null)

        assertFailure(service.getMeme("guild", "missing"))
    }

    @Test
    fun `getMemerIds aggregates users and sorts by descending score`() {
        val memes =
            listOf(
                meme("alice-1", userId = "alice", upvotes = 3, downvotes = 1),
                meme("alice-2", userId = "alice", upvotes = 2),
                meme("bob", userId = "bob", upvotes = 3),
                meme("negative", userId = "negative", downvotes = 1),
            )
        `when`(springGuildService.getGuild("guild")).thenReturn(Success(SpringGuild("guild")))
        `when`(mongoTemplate.find(any(Query::class.java), eq(Meme::class.java))).thenReturn(memes)

        val rankings = assertSuccess(service.getMemerIds("guild")).value

        assertEquals(listOf("alice", "bob"), rankings.map { it.first })
        assertEquals(4, rankings[0].second.score)
        assertEquals(5, rankings[0].second.upvotes)
        assertEquals(1, rankings[0].second.downvotes)
        assertEquals(2, rankings[0].second.count)
        verify(springGuildService).getGuild("guild")
    }

    @Test
    fun `getMemerIds sorts ascending and includes non-positive scores`() {
        val memes =
            listOf(
                meme("zero", userId = "zero", upvotes = 1, downvotes = 1),
                meme("positive", userId = "positive", upvotes = 2),
                meme("negative", userId = "negative", downvotes = 1),
            )
        `when`(springGuildService.getGuild("guild")).thenReturn(Success(SpringGuild("guild")))
        `when`(mongoTemplate.find(any(Query::class.java), eq(Meme::class.java))).thenReturn(memes)

        val rankings = assertSuccess(service.getMemerIds("guild", asc = true)).value

        assertEquals(listOf("negative", "zero", "positive"), rankings.map { it.first })
    }

    @Test
    fun `getMemerIds propagates guild lookup failure`() {
        `when`(springGuildService.getGuild("missing")).thenReturn(Failure("Guild not found"))

        assertFailure(service.getMemerIds("missing"))

        verify(mongoTemplate, never()).find(any(Query::class.java), eq(Meme::class.java))
    }

    @Test
    fun `getTop3ByInterval omits unvoted memes and sorts by score`() {
        val memes =
            listOf(
                meme("fourth", upvotes = 2),
                meme("unvoted"),
                meme("first", upvotes = 5),
                meme("third", upvotes = 3),
                meme("second", upvotes = 4),
            )
        `when`(mongoTemplate.find(any(Query::class.java), eq(Meme::class.java))).thenReturn(memes)

        val topMemes = assertSuccess(service.getTop3ByInterval("guild", MemeService.MemeInterval.WEEK)).value

        assertEquals(listOf("first", "second", "third"), topMemes.map { it.messageId })
    }

    @Test
    fun `getTop3ByInterval supports month interval`() {
        val memes = listOf(meme("first", upvotes = 2))
        `when`(mongoTemplate.find(any(Query::class.java), eq(Meme::class.java))).thenReturn(memes)

        assertEquals(
            listOf("first"),
            assertSuccess(service.getTop3ByInterval("guild", MemeService.MemeInterval.MONTH)).value.map { it.messageId },
        )
    }

    @Test
    fun `addUpvote adds voter to meme`() {
        assertVoteUpdate(
            userId = "voter",
            updateOperation = "\$addToSet",
            voterField = "upvoters",
        ) { guildId, messageId, userId -> service.addUpvote(guildId, messageId, userId) }
    }

    @Test
    fun `addDownvote adds voter to meme`() {
        assertVoteUpdate(
            userId = "voter",
            updateOperation = "\$addToSet",
            voterField = "downvoters",
        ) { guildId, messageId, userId -> service.addDownvote(guildId, messageId, userId) }
    }

    @Test
    fun `removeUpvote removes voter from meme`() {
        assertVoteUpdate(
            userId = "voter",
            updateOperation = "\$pull",
            voterField = "upvoters",
        ) { guildId, messageId, userId -> service.removeUpvote(guildId, messageId, userId) }
    }

    @Test
    fun `removeDownvote removes voter from meme`() {
        assertVoteUpdate(
            userId = "voter",
            updateOperation = "\$pull",
            voterField = "downvoters",
        ) { guildId, messageId, userId -> service.removeDownvote(guildId, messageId, userId) }
    }

    @Test
    fun `vote updates return failure when meme does not exist`() {
        `when`(memeRepository.findByGuildIdAndMessageId("guild", "missing")).thenReturn(null)

        assertFailure(service.addUpvote("guild", "missing", "voter"))
        assertFailure(service.addDownvote("guild", "missing", "voter"))
        assertFailure(service.removeUpvote("guild", "missing", "voter"))
        assertFailure(service.removeDownvote("guild", "missing", "voter"))

        verify(mongoTemplate, never()).findAndModify(any(Query::class.java), any(Update::class.java), eq(Meme::class.java))
    }

    @Test
    fun `vote updates return failure when meme disappears before update`() {
        val meme = meme("message")
        `when`(memeRepository.findByGuildIdAndMessageId("guild", "message")).thenReturn(meme)
        `when`(
            mongoTemplate.findAndModify(
                any(Query::class.java),
                any(Update::class.java),
                eq(Meme::class.java),
            ),
        ).thenReturn(null)

        assertFailure(service.addUpvote("guild", "message", "voter"))
    }

    private fun assertVoteUpdate(
        userId: String,
        updateOperation: String,
        voterField: String,
        updateVote: (String, String, String) -> OperationResult<Unit, String>,
    ) {
        val meme = meme("message")
        `when`(memeRepository.findByGuildIdAndMessageId("guild", "message")).thenReturn(meme)
        `when`(
            mongoTemplate.findAndModify(
                any(Query::class.java),
                any(Update::class.java),
                eq(Meme::class.java),
            ),
        ).thenReturn(meme)

        assertSuccess(updateVote("guild", "message", userId))

        val updateCaptor = ArgumentCaptor.forClass(Update::class.java)
        verify(mongoTemplate).findAndModify(
            any(Query::class.java),
            updateCaptor.capture(),
            eq(Meme::class.java),
        )
        assertEquals(
            Document(updateOperation, Document(voterField, userId)),
            updateCaptor.value.updateObject,
        )
    }

    private fun meme(
        messageId: String,
        userId: String = "user",
        upvotes: Int = 0,
        downvotes: Int = 0,
    ): Meme = Meme(
        messageId = messageId,
        guildId = "guild",
        userId = userId,
        url = "https://example.com/$messageId",
        channelId = "channel",
    ).apply {
        id = "id-$messageId"
        upvoters = (1..upvotes).map { "upvoter-$it" }
        downvoters = (1..downvotes).map { "downvoter-$it" }
    }
}
