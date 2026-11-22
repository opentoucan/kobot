package uk.me.danielharman.kotlinspringbot.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.properties.YoutubeDlProperties
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Duration
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.setLastModifiedTime

data class CachedAudio(
    val file: Path,
    val videoId: String,
    val title: String,
    val uploader: String,
    val duration: Duration,
    val url: String,
    val fromCache: Boolean,
)

/**
 * Downloads the audio of YouTube videos with yt-dlp into a size-limited on-disk cache, so that
 * lavaplayer can play them as local files.
 */
@Service
class YoutubeAudioService(
    private val properties: YoutubeDlProperties,
    private val processRunner: ProcessRunner,
    private val audioFileUsage: AudioFileUsage,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val cacheDirectory: Path = Path.of(properties.cacheDirectory).toAbsolutePath().normalize()

    // Stops two requests for the same video downloading to the same file at once. A fixed set of
    // locks shared by video id, so memory doesn't grow with every video ever requested.
    private val downloadLocks = Array(DOWNLOAD_LOCK_COUNT) { ReentrantLock() }
    private val evictionLock = ReentrantLock()

    private data class VideoMetadata(
        val id: String,
        val title: String,
        val uploader: String,
        val duration: Duration,
    )

    /**
     * Resolves [query] (a YouTube url or search terms) to a single video and returns its audio,
     * downloading it first if it isn't already cached.
     */
    fun fetchAudio(query: String): OperationResult<CachedAudio, String> {
        val target =
            when (val t = toYtDlpTarget(query)) {
                is Failure -> return t
                is Success -> t.value
            }

        val metadata =
            when (val m = fetchMetadata(target)) {
                is Failure -> return m
                is Success -> m.value
            }

        if (metadata.duration > properties.maxTrackDuration) {
            return Failure(
                "${metadata.title} is longer than the ${properties.maxTrackDuration.toMinutes()} minute limit",
            )
        }

        try {
            Files.createDirectories(cacheDirectory)
        } catch (e: IOException) {
            logger.error("Could not create audio cache directory $cacheDirectory", e)
            return Failure("Could not create the audio cache directory")
        }

        val lock = downloadLocks[Math.floorMod(metadata.id.hashCode(), DOWNLOAD_LOCK_COUNT)]
        return lock.withLock {
            val cached = findCachedFile(metadata.id)?.takeIf { markUsed(it) }
            if (cached != null) {
                Success(toCachedAudio(metadata, cached, fromCache = true))
            } else {
                when (val downloaded = download(metadata.id)) {
                    is Failure -> downloaded

                    is Success -> {
                        markUsed(downloaded.value)
                        evictCache(keep = downloaded.value)
                        Success(toCachedAudio(metadata, downloaded.value, fromCache = false))
                    }
                }
            }
        }
    }

    /**
     * Whether [query] is something [fetchAudio] handles: a YouTube url or search terms, rather than a
     * link to another site.
     */
    fun isYoutubeQuery(query: String): Boolean {
        val trimmed = query.trim()
        return !isHttpUrl(trimmed) || isYoutubeUrl(trimmed)
    }

    private fun toYtDlpTarget(query: String): OperationResult<String, String> {
        val trimmed = query.trim()
        return when {
            trimmed.isEmpty() -> Failure("Provide a YouTube url or something to search for")
            !isHttpUrl(trimmed) -> Success("ytsearch1:$trimmed")
            isYoutubeUrl(trimmed) -> Success(trimmed)
            else -> Failure("Only YouTube links are supported")
        }
    }

    private fun isHttpUrl(query: String) = query.startsWith("http://", ignoreCase = true) || query.startsWith("https://", ignoreCase = true)

    private fun isYoutubeUrl(url: String): Boolean {
        val host =
            try {
                URI(url).host?.lowercase()
            } catch (_: URISyntaxException) {
                null
            }
        return host != null && host in YOUTUBE_HOSTS
    }

    private fun fetchMetadata(target: String): OperationResult<VideoMetadata, String> {
        val result =
            runYtDlp(
                listOf(
                    "--skip-download",
                    // Playlist links and searches would otherwise resolve every entry before printing
                    "--playlist-items",
                    "1",
                    "--print",
                    "%(id)s\t%(duration)s\t%(is_live)s\t%(uploader)s\t%(title)s",
                    "--",
                    target,
                ),
            )
        return when (result) {
            is Failure -> result
            is Success -> parseMetadata(result.value)
        }
    }

    private fun parseMetadata(stdout: String): OperationResult<VideoMetadata, String> {
        val line =
            stdout.lineSequence().firstOrNull { it.isNotBlank() }
                ?: return Failure("No YouTube video found")
        val fields = line.split('\t', limit = METADATA_FIELD_COUNT)
        val id = fields.first()
        if (fields.size != METADATA_FIELD_COUNT || !VIDEO_ID_PATTERN.matches(id)) {
            logger.error("Unexpected yt-dlp metadata output: $line")
            return Failure("Could not read the video's details")
        }

        val title = fields[TITLE_FIELD]
        val seconds = fields[DURATION_FIELD].toDoubleOrNull()
        return when {
            fields[IS_LIVE_FIELD] == "True" -> Failure("Live streams can't be downloaded")

            seconds == null -> Failure("Could not work out how long $title is")

            else ->
                Success(
                    VideoMetadata(
                        id = id,
                        title = title,
                        uploader = fields[UPLOADER_FIELD],
                        duration = Duration.ofMillis((seconds * MILLIS_PER_SECOND).toLong()),
                    ),
                )
        }
    }

    private fun download(videoId: String): OperationResult<Path, String> {
        logger.info("Downloading audio for YouTube video $videoId")
        val result =
            runYtDlp(
                listOf(
                    "--no-simulate",
                    "--no-progress",
                    // Formats lavaplayer can play directly without re-encoding
                    "-f",
                    "bestaudio[ext=webm]/bestaudio[ext=m4a]/bestaudio",
                    "-o",
                    cacheDirectory.resolve("$videoId.%(ext)s").toString(),
                    "--print",
                    "after_move:filepath",
                    "--",
                    videoUrl(videoId),
                ),
            )
        val stdout =
            when (result) {
                is Failure -> return result
                is Success -> result.value
            }

        val file =
            stdout
                .lineSequence()
                .lastOrNull { it.isNotBlank() }
                ?.let { Path.of(it.trim()).toAbsolutePath().normalize() }

        if (file == null || file.parent != cacheDirectory || !file.isRegularFile()) {
            logger.error("yt-dlp reported an unexpected download location: $stdout")
            return Failure("Downloading the audio failed")
        }

        return Success(file)
    }

    private fun runYtDlp(arguments: List<String>): OperationResult<String, String> {
        val command = listOf(properties.executable, "--no-playlist", "--no-warnings") + arguments

        val result =
            when (val run = processRunner.run(command, properties.timeout)) {
                is Failure -> {
                    logger.error("Failed to run yt-dlp: ${run.reason}")
                    return Failure(run.reason)
                }

                is Success -> run.value
            }

        if (result.exitCode != 0) {
            logger.error("yt-dlp exited with code ${result.exitCode}: ${result.stderr}")
            val error =
                result.stderr.lineSequence().lastOrNull { it.startsWith("ERROR:") }
                    ?: "yt-dlp exited with code ${result.exitCode}"
            return Failure(error)
        }

        return Success(result.stdout)
    }

    private fun findCachedFile(videoId: String): Path? = cachedFiles().firstOrNull { it.nameWithoutExtension == videoId }

    // Cached files are evicted least recently used first. Returns false if the file has just been evicted.
    private fun markUsed(file: Path): Boolean = try {
        file.setLastModifiedTime(FileTime.fromMillis(System.currentTimeMillis()))
        true
    } catch (_: IOException) {
        false
    }

    // Only finished downloads; yt-dlp's partial and temporary files have other extensions
    private fun cachedFiles(): List<Path> = cacheDirectory.listDirectoryEntries().filter {
        it.isRegularFile() && it.extension in AUDIO_EXTENSIONS
    }

    /**
     * Deletes the least recently used files until the cache fits in its size limit. Files that are
     * playing, queued or were used in the last minute (so may be about to be queued) are kept, even if
     * that leaves the cache over its limit.
     */
    private fun evictCache(keep: Path) {
        evictionLock.withLock {
            try {
                val files = cachedFiles().sortedBy { it.getLastModifiedTime() }
                var totalSize = files.sumOf { it.fileSize() }

                val inUse = audioFileUsage.filesInUse()
                val recentlyUsed = FileTime.fromMillis(System.currentTimeMillis() - RECENT_USE_GRACE.toMillis())
                val evictable =
                    files.filter { it != keep && it !in inUse && it.getLastModifiedTime() < recentlyUsed }

                for (file in evictable) {
                    if (totalSize <= properties.maxCacheSize.toBytes()) break

                    val size = file.fileSize()
                    if (Files.deleteIfExists(file)) {
                        logger.info("Evicted ${file.name} from the audio cache")
                        totalSize -= size
                    }
                }
            } catch (e: IOException) {
                // The download itself succeeded, so a failed clean up shouldn't stop it playing
                logger.error("Failed to evict files from the audio cache", e)
            }
        }
    }

    private fun toCachedAudio(
        metadata: VideoMetadata,
        file: Path,
        fromCache: Boolean,
    ) = CachedAudio(
        file = file,
        videoId = metadata.id,
        title = metadata.title,
        uploader = metadata.uploader,
        duration = metadata.duration,
        url = videoUrl(metadata.id),
        fromCache = fromCache,
    )

    private fun videoUrl(videoId: String) = "https://www.youtube.com/watch?v=$videoId"

    companion object {
        // Fields of the --print template used in fetchMetadata
        private const val METADATA_FIELD_COUNT = 5
        private const val DURATION_FIELD = 1
        private const val IS_LIVE_FIELD = 2
        private const val UPLOADER_FIELD = 3
        private const val TITLE_FIELD = 4
        private const val MILLIS_PER_SECOND = 1000
        private const val DOWNLOAD_LOCK_COUNT = 64

        private val RECENT_USE_GRACE: Duration = Duration.ofMinutes(1)

        private val YOUTUBE_HOSTS =
            setOf("youtube.com", "www.youtube.com", "m.youtube.com", "music.youtube.com", "youtu.be")

        private val VIDEO_ID_PATTERN = Regex("^[A-Za-z0-9_-]{11}$")

        private val AUDIO_EXTENSIONS = setOf("webm", "m4a", "mp4", "mp3", "ogg", "opus")
    }
}
