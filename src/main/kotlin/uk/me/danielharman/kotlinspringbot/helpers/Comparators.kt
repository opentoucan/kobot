package uk.me.danielharman.kotlinspringbot.helpers

object Comparators {
    val mapStrIntComparator =
        Comparator {
            entry1: MutableMap.MutableEntry<String, Int>,
            entry2: MutableMap.MutableEntry<String, Int>,
            ->
            entry2.value - entry1.value
        }
}
