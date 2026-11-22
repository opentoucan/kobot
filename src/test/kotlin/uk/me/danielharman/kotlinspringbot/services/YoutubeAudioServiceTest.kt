package uk.me.danielharman.kotlinspringbot.services

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.util.unit.DataSize
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.Success
import uk.me.danielharman.kotlinspringbot.helpers.assertFailure
import uk.me.danielharman.kotlinspringbot.helpers.assertSuccess
import uk.me.danielharman.kotlinspringbot.properties.YoutubeDlProperties
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Duration
import kotlin.io.path.exists
import kotlin.io.path.setLastModifiedTime
import kotlin.io.path.writeBytes

private const val VIDEO_ID = "jNQXAC9IVRw"

internal class YoutubeAudioServiceTest {
    @TempDir
    private lateinit var cacheDirectory: Path

    private val commands = mutableListOf<List<String>>()
    private var answer: (List<String>) -> OperationResult<ProcessResult, String> = { error("yt-dlp was not stubbed") }

    // Records each command and answers it with [answer]
    private val processRunner =
        object : ProcessRunner() {
            override fun run(
                command: List<String>,
                timeout: Duration,
            ): OperationResult<ProcessResult, String> {
                commands.add(command)
                return answer(command)
            }
        }

    private var filesInUse: Set<Path> = emptySet()

    private lateinit var service: YoutubeAudioService

    @BeforeEach
    fun setUp() {
        service = createService()
    }

    private fun createService(maxCacheSize: DataSize = DataSize.ofMegabytes(1)) = YoutubeAudioService(
        YoutubeDlProperties(
            cacheDirectory = cacheDirectory.toString(),
            maxCacheSize = maxCacheSize,
            maxTrackDuration = Duration.ofMinutes(10),
        ),
        processRunner,
        { filesInUse },
    )

    private fun metadata(
        id: String = VIDEO_ID,
        duration: String = "19",
        isLive: String = "False",
    ) = Success(ProcessResult(0, "$id\t$duration\t$isLive\tjawed\tMe at the zoo\n", ""))

    // Answers yt-dlp calls: metadata lookups print the details, downloads create the file
    private fun stubYtDlp(
        metadataResult: Success<ProcessResult> = metadata(),
        downloadSize: Int = 10,
    ) {
        answer = { command ->
            if ("--skip-download" in command) {
                metadataResult
            } else {
                val id = command.last().substringAfter("v=")
                val file = cacheDirectory.resolve("$id.webm")
                file.writeBytes(ByteArray(downloadSize))
                Success(ProcessResult(0, "$file\n", ""))
            }
        }
    }

    @Test
    fun shouldDownloadAndReturnVideoDetails() {
        stubYtDlp()

        val audio = assertSuccess(service.fetchAudio("https://www.youtube.com/watch?v=$VIDEO_ID")).value

        assertThat(audio.videoId, equalTo(VIDEO_ID))
        assertThat(audio.title, equalTo("Me at the zoo"))
        assertThat(audio.uploader, equalTo("jawed"))
        assertThat(audio.duration, equalTo(Duration.ofSeconds(19)))
        assertThat(audio.file, equalTo(cacheDirectory.resolve("$VIDEO_ID.webm")))
        assertThat(audio.fromCache, `is`(false))
    }

    @Test
    fun shouldUseCachedFileOnSecondRequest() {
        stubYtDlp()

        service.fetchAudio("https://youtu.be/$VIDEO_ID")
        val audio = assertSuccess(service.fetchAudio("https://youtu.be/$VIDEO_ID")).value

        assertThat(audio.fromCache, `is`(true))
        // Two metadata lookups and a single download
        assertThat(commands.size, equalTo(3))
    }

    @Test
    fun shouldSearchYoutubeForNonUrlQueries() {
        stubYtDlp()

        assertSuccess(service.fetchAudio("me at the zoo"))

        assertThat(commands[0], hasItem("ytsearch1:me at the zoo"))
        // Only the first entry of a search or playlist is resolved
        assertThat(commands[0].indexOf("--playlist-items"), equalTo(commands[0].indexOf("1") - 1))
        // The download uses the resolved video rather than searching again
        assertThat(commands[1].last(), equalTo("https://www.youtube.com/watch?v=$VIDEO_ID"))
    }

    @Test
    fun shouldPassQueryAfterOptionTerminator() {
        stubYtDlp()

        assertSuccess(service.fetchAudio("--exec rm"))

        val command = commands[0]
        assertThat(command.indexOf("--"), equalTo(command.size - 2))
        assertThat(command.last(), equalTo("ytsearch1:--exec rm"))
    }

    @Test
    fun shouldRejectNonYoutubeUrls() {
        val result = assertFailure(service.fetchAudio("https://example.com/video.mp3"))

        assertThat(result.reason, equalTo("Only YouTube links are supported"))
        assertThat(commands.size, equalTo(0))
    }

    @Test
    fun shouldTreatYoutubeUrlsAndSearchTermsAsYoutubeQueries() {
        assertThat(service.isYoutubeQuery("https://www.youtube.com/watch?v=$VIDEO_ID"), `is`(true))
        assertThat(service.isYoutubeQuery("https://youtu.be/$VIDEO_ID"), `is`(true))
        assertThat(service.isYoutubeQuery("HTTPS://MUSIC.YOUTUBE.COM/watch?v=$VIDEO_ID"), `is`(true))
        assertThat(service.isYoutubeQuery("me at the zoo"), `is`(true))
        assertThat(service.isYoutubeQuery("  https://vimeo.com/76979871"), `is`(false))
        assertThat(service.isYoutubeQuery("https://soundcloud.com/artist/track"), `is`(false))
        assertThat(service.isYoutubeQuery("https://example.com/youtube.com/audio.mp3"), `is`(false))
    }

    @Test
    fun shouldRejectBlankQueries() {
        assertFailure(service.fetchAudio("   "))
        assertThat(commands.size, equalTo(0))
    }

    @Test
    fun shouldRejectVideosOverMaxDuration() {
        stubYtDlp(metadata(duration = "601"))

        val result = assertFailure(service.fetchAudio("long video"))

        assertThat(result.reason, containsString("10 minute limit"))
        assertThat(commands.size, equalTo(1))
    }

    @Test
    fun shouldRejectLiveStreams() {
        stubYtDlp(metadata(duration = "NA", isLive = "True"))

        val result = assertFailure(service.fetchAudio("live stream"))

        assertThat(result.reason, equalTo("Live streams can't be downloaded"))
    }

    @Test
    fun shouldRejectUnexpectedVideoIds() {
        stubYtDlp(metadata(id = "../../etc/passwd"))

        assertFailure(service.fetchAudio("something"))
        assertThat(commands.size, equalTo(1))
    }

    @Test
    fun shouldReportYtDlpErrors() {
        answer = { Success(ProcessResult(1, "", "WARNING: something\nERROR: [youtube] abc: Video unavailable\n")) }

        val result = assertFailure(service.fetchAudio("https://www.youtube.com/watch?v=abc"))

        assertThat(result.reason, equalTo("ERROR: [youtube] abc: Video unavailable"))
    }

    @Test
    fun shouldReportProcessFailures() {
        answer = { Failure("Could not start yt-dlp: not found") }

        val result = assertFailure(service.fetchAudio("anything"))

        assertThat(result.reason, equalTo("Could not start yt-dlp: not found"))
    }

    @Test
    fun shouldEvictLeastRecentlyUsedFilesWhenCacheIsFull() {
        service = createService(maxCacheSize = DataSize.ofBytes(25))
        val oldest = cacheDirectory.resolve("aaaaaaaaaaa.webm")
        val newer = cacheDirectory.resolve("bbbbbbbbbbb.m4a")
        val partial = cacheDirectory.resolve("ccccccccccc.webm.part")
        oldest.writeBytes(ByteArray(10))
        newer.writeBytes(ByteArray(10))
        partial.writeBytes(ByteArray(100))
        oldest.setLastModifiedTime(FileTime.fromMillis(1_000))
        newer.setLastModifiedTime(FileTime.fromMillis(2_000))
        stubYtDlp(downloadSize = 10)

        assertSuccess(service.fetchAudio("https://www.youtube.com/watch?v=$VIDEO_ID"))

        assertThat(oldest.exists(), `is`(false))
        assertThat(newer.exists(), `is`(true))
        assertThat(cacheDirectory.resolve("$VIDEO_ID.webm").exists(), `is`(true))
        // In-progress downloads are left alone
        assertThat(partial.exists(), `is`(true))
    }

    @Test
    fun shouldNotEvictFilesThatArePlayingOrQueued() {
        service = createService(maxCacheSize = DataSize.ofBytes(15))
        val queued = cacheDirectory.resolve("aaaaaaaaaaa.webm")
        val unused = cacheDirectory.resolve("bbbbbbbbbbb.webm")
        queued.writeBytes(ByteArray(10))
        unused.writeBytes(ByteArray(10))
        queued.setLastModifiedTime(FileTime.fromMillis(1_000))
        unused.setLastModifiedTime(FileTime.fromMillis(2_000))
        filesInUse = setOf(queued)
        stubYtDlp(downloadSize = 1)

        assertSuccess(service.fetchAudio("https://www.youtube.com/watch?v=$VIDEO_ID"))

        assertThat(queued.exists(), `is`(true))
        assertThat(unused.exists(), `is`(false))
    }

    @Test
    fun shouldNotEvictRecentlyUsedFiles() {
        service = createService(maxCacheSize = DataSize.ofBytes(5))
        val recent = cacheDirectory.resolve("aaaaaaaaaaa.webm")
        recent.writeBytes(ByteArray(10))
        stubYtDlp(downloadSize = 10)

        assertSuccess(service.fetchAudio("https://www.youtube.com/watch?v=$VIDEO_ID"))

        // Just written, so it may be about to be queued; the cache is left over its limit instead
        assertThat(recent.exists(), `is`(true))
    }

    @Test
    fun shouldCreateCacheDirectory() {
        Files.delete(cacheDirectory)
        stubYtDlp()

        assertSuccess(service.fetchAudio("https://www.youtube.com/watch?v=$VIDEO_ID"))

        assertThat(cacheDirectory.exists(), `is`(true))
    }
}
