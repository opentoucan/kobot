package uk.me.danielharman.kotlinspringbot.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.util.unit.DataSize
import java.time.Duration

private const val DEFAULT_TIMEOUT_MINUTES = 5L

@ConfigurationProperties(prefix = "youtube-dl")
data class YoutubeDlProperties(
    // yt-dlp executable name or path
    val executable: String = "yt-dlp",
    // Directory downloaded audio is cached in
    val cacheDirectory: String = "audio-cache",
    // Least recently played files are deleted once the cache grows beyond this size
    val maxCacheSize: DataSize = DataSize.ofGigabytes(1),
    // Longest video that will be downloaded
    val maxTrackDuration: Duration = Duration.ofHours(1),
    // How long a single yt-dlp invocation may run before it is killed
    val timeout: Duration = Duration.ofMinutes(DEFAULT_TIMEOUT_MINUTES),
)
