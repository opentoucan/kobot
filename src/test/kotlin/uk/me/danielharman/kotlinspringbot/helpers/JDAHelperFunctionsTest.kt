package uk.me.danielharman.kotlinspringbot.helpers

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.utils.concurrent.Task
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyCollection
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

internal class JDAHelperFunctionsTest {
    @Test
    fun shouldRetrieveMembersInBatchesOfAtMostTheRequestLimit() {
        val guild = mock(Guild::class.java)
        val requestedBatches = mutableListOf<List<Long>>()

        `when`(guild.retrieveMembersByIds(anyCollection<Long>())).thenAnswer { invocation ->
            val ids = invocation.getArgument<Collection<Long>>(0).toList()
            requestedBatches.add(ids)
            val members =
                ids.map { id ->
                    val member = mock(Member::class.java)
                    `when`(member.id).thenReturn(id.toString())
                    member
                }
            val task = mock(Task::class.java)
            `when`(task.get()).thenReturn(members)
            task
        }

        val ids = (1L..250L).map { it.toString() }
        val members = JDAHelperFunctions.retrieveMembersById(guild, ids)

        assertEquals(listOf(100, 100, 50), requestedBatches.map { it.size })
        assertTrue(requestedBatches.all { it.size <= MEMBER_REQUEST_LIMIT })
        assertEquals(ids.toSet(), members.keys)
    }

    @Test
    fun shouldNotRequestAnythingForNoIds() {
        val guild = mock(Guild::class.java)

        val members = JDAHelperFunctions.retrieveMembersById(guild, listOf())

        assertTrue(members.isEmpty())
    }
}
