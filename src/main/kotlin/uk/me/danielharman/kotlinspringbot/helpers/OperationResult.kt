package uk.me.danielharman.kotlinspringbot.helpers

sealed class OperationResult<out T, out E>

data class Success<out T>(
    val value: T,
) : OperationResult<T, Nothing>()

data class Failure<out E>(
    val reason: E,
) : OperationResult<Nothing, E>()

/** Transforms the value of a [Success], passing a [Failure] through unchanged. */
inline fun <T, E, R> OperationResult<T, E>.map(transform: (T) -> R): OperationResult<R, E> = when (this) {
    is Success -> Success(transform(value))
    is Failure -> this
}

/** Chains another operation onto a [Success], passing a [Failure] through unchanged. */
inline fun <T, E, R> OperationResult<T, E>.flatMap(transform: (T) -> OperationResult<R, E>): OperationResult<R, E> = when (this) {
    is Success -> transform(value)
    is Failure -> this
}

/** Returns the value of a [Success], or the result of [onFailure] for a [Failure]. */
inline fun <T, E> OperationResult<T, E>.getOrElse(onFailure: (E) -> T): T = when (this) {
    is Success -> value
    is Failure -> onFailure(reason)
}

/** Runs [action] with the value of a [Success], then returns this result. */
inline fun <T, E> OperationResult<T, E>.onSuccess(action: (T) -> Unit): OperationResult<T, E> {
    if (this is Success) action(value)
    return this
}

/** Runs [action] with the reason of a [Failure], then returns this result. */
inline fun <T, E> OperationResult<T, E>.onFailure(action: (E) -> Unit): OperationResult<T, E> {
    if (this is Failure) action(reason)
    return this
}
