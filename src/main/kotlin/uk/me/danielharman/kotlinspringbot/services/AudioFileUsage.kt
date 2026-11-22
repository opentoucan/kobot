package uk.me.danielharman.kotlinspringbot.services

import java.nio.file.Path

/**
 * Reports which local audio files are playing or queued, so cached files aren't deleted from under
 * the player.
 */
fun interface AudioFileUsage {
    fun filesInUse(): Set<Path>
}
