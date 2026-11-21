package uk.me.danielharman.kotlinspringbot.services

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import uk.me.danielharman.kotlinspringbot.helpers.assertFailure
import uk.me.danielharman.kotlinspringbot.helpers.assertSuccess
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.io.path.readText

internal class ProcessRunnerTest {
    private val processRunner = ProcessRunner()

    @Test
    fun shouldCaptureOutputAndExitCode() {
        val result =
            assertSuccess(
                processRunner.run(listOf("sh", "-c", "echo out; echo err >&2; exit 3"), Duration.ofSeconds(10)),
            ).value

        assertThat(result.exitCode, equalTo(3))
        assertThat(result.stdout, equalTo("out\n"))
        assertThat(result.stderr, equalTo("err\n"))
    }

    @Test
    fun shouldNotWaitForInput() {
        val result = assertSuccess(processRunner.run(listOf("cat"), Duration.ofSeconds(10))).value

        assertThat(result.exitCode, equalTo(0))
    }

    @Test
    fun shouldFailWhenProcessTimesOut() {
        val result = assertFailure(processRunner.run(listOf("sleep", "10"), Duration.ofMillis(200)))

        assertThat(result.reason, containsString("did not finish"))
    }

    @Test
    fun shouldKillChildProcessesWhenProcessTimesOut(
        @TempDir directory: Path,
    ) {
        val pidFile = directory.resolve("child.pid")

        assertFailure(
            processRunner.run(listOf("sh", "-c", "sleep 60 & echo \$! > $pidFile; wait"), Duration.ofSeconds(2)),
        )

        val child = ProcessHandle.of(pidFile.readText().trim().toLong())
        child.ifPresent { it.onExit().get(5, TimeUnit.SECONDS) }
        assertThat(child.map { it.isAlive }.orElse(false), `is`(false))
    }

    @Test
    fun shouldFailWhenExecutableIsMissing() {
        val result = assertFailure(processRunner.run(listOf("kobot-missing-executable"), Duration.ofSeconds(10)))

        assertThat(result.reason, containsString("Could not start kobot-missing-executable"))
    }
}
