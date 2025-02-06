package uk.me.danielharman.kotlinspringbot.helpers

import org.apache.commons.lang3.time.DurationFormatUtils

object HelperFunctions {
    fun <A, B, C> partialWrapper(f: (A, B) -> C, a: A): (B) -> C {
        return { b: B -> f(a, b)}
    }

    private const val TIME_FORMAT = "mm:ss"
    fun Long.formatDurationString(): String {
        return DurationFormatUtils.formatDuration(this, TIME_FORMAT)
    }

}