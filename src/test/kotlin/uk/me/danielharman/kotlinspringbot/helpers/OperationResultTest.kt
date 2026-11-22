package uk.me.danielharman.kotlinspringbot.helpers

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

internal class OperationResultTest {
    private val success: OperationResult<Int, String> = Success(2)
    private val failure: OperationResult<Int, String> = Failure("broken")

    @Test
    fun mapShouldTransformSuccess() {
        assertThat(success.map { it * 3 }, `is`(Success(6)))
    }

    @Test
    fun mapShouldPassFailureThroughWithoutCallingTransform() {
        var called = false
        val result = failure.map { called = true }

        assertThat(result, `is`(Failure("broken")))
        assertThat(called, `is`(false))
    }

    @Test
    fun flatMapShouldChainSuccess() {
        assertThat(success.flatMap { Success("value $it") }, `is`(Success("value 2")))
    }

    @Test
    fun flatMapShouldReturnFailureFromTransform() {
        assertThat(success.flatMap { Failure("second step failed") }, `is`(Failure("second step failed")))
    }

    @Test
    fun flatMapShouldPassFailureThroughWithoutCallingTransform() {
        var called = false
        val result =
            failure.flatMap {
                called = true
                Success(it)
            }

        assertThat(result, `is`(Failure("broken")))
        assertThat(called, `is`(false))
    }

    @Test
    fun getOrElseShouldReturnValueForSuccess() {
        assertThat(success.getOrElse { 0 }, `is`(2))
    }

    @Test
    fun getOrElseShouldUseFallbackWithReasonForFailure() {
        assertThat(failure.getOrElse { it.length }, `is`(6))
    }

    @Test
    fun onSuccessShouldRunOnlyForSuccess() {
        val seen = mutableListOf<Int>()

        assertThat(success.onSuccess { seen.add(it) }, `is`(success))
        failure.onSuccess { seen.add(it) }

        assertThat(seen, `is`(listOf(2)))
    }

    @Test
    fun onFailureShouldRunOnlyForFailure() {
        val seen = mutableListOf<String>()

        assertThat(failure.onFailure { seen.add(it) }, `is`(failure))
        success.onFailure { seen.add(it) }

        assertThat(seen, `is`(listOf("broken")))
    }
}
