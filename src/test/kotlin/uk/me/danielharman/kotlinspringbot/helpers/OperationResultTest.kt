package uk.me.danielharman.kotlinspringbot.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OperationResultTest {
    @Test
    fun `fold handles successful result`() {
        val result: OperationResult<Int, String> = Success(2)

        assertEquals("success: 2", result.fold({ "success: $it" }, { "failure: $it" }))
    }

    @Test
    fun `fold handles failure result`() {
        val result: OperationResult<Int, String> = Failure("not found")

        assertEquals("failure: not found", result.fold({ "success: $it" }, { "failure: $it" }))
    }

    @Test
    fun `map transforms successful value`() {
        val result: OperationResult<Int, String> = Success(2)

        assertEquals(Success(4), result.map { it * 2 })
    }

    @Test
    fun `map preserves failure`() {
        val result: OperationResult<Int, String> = Failure("not found")

        assertEquals(Failure("not found"), result.map { it * 2 })
    }

    @Test
    fun `flatMap chains successful results`() {
        val result: OperationResult<Int, String> = Success(2)

        assertEquals(Success("value: 2"), result.flatMap { Success("value: $it") })
    }

    @Test
    fun `flatMap preserves existing failure`() {
        val result: OperationResult<Int, String> = Failure("not found")

        assertEquals(Failure("not found"), result.flatMap { Success("value: $it") })
    }

    @Test
    fun `flatMap returns failure from transform`() {
        val result: OperationResult<Int, String> = Success(2)

        assertEquals(Failure("invalid value"), result.flatMap { Failure("invalid value") })
    }
}
