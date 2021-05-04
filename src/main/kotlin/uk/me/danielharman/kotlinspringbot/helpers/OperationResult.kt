package uk.me.danielharman.kotlinspringbot.helpers

sealed class OperationResult<out Success, out Failure>

data class Success<out Success>(val value: Success) : OperationResult<Success, Nothing>()
data class Failure<out Failure>(val reason: Failure) : OperationResult<Nothing, Failure>()