package uk.me.danielharman.kotlinspringbot.helpers

import io.kotest.assertions.fail

fun <T, U> assertSuccess(
    op: OperationResult<T, U>,
    msg: String = "Expected success result was failure"
): Success<T> {
    if (op is Failure) {
        fail(if (op.reason is String) op.reason as String else msg)
    }
    return op as Success<T>
}

fun <T, U> assertFailure(
    op: OperationResult<T, U>,
    msg: String = "Expected failure result was success"
): Failure<U> {
    if (op is Success) {
        fail(msg)
    }
    return op as Failure<U>
}
