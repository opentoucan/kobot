package uk.me.danielharman.kotlinspringbot.helpers

object HelperFunctions {
    fun <A, B, C> partialWrapper(
        f: (A, B) -> C,
        a: A,
    ): (B) -> C = { b: B -> f(a, b) }
}
