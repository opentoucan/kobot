@file:kotlin.jvm.JvmName("OperationResultExtensions")

package uk.me.danielharman.kotlinspringbot.helpers

sealed class OperationResult<out T, out E>

data class Success<out T>(
    val value: T,
) : OperationResult<T, Nothing>()

data class Failure<out E>(
    val reason: E,
) : OperationResult<Nothing, E>()

fun <T, E, R> OperationResult<T, E>.fold(
    onSuccess: (T) -> R,
    onFailure: (E) -> R,
): R = when (this) {
    is Success -> onSuccess(value)
    is Failure -> onFailure(reason)
}

fun <T, E, R> OperationResult<T, E>.map(transform: (T) -> R): OperationResult<R, E> = when (this) {
    is Success -> Success(transform(value))
    is Failure -> Failure(reason)
}

fun <T, E, R> OperationResult<T, E>.flatMap(
    transform: (T) -> OperationResult<R, E>,
): OperationResult<R, E> = when (this) {
    is Success -> transform(value)
    is Failure -> Failure(reason)
}
