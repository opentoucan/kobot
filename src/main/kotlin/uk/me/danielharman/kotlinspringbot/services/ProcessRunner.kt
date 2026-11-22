package uk.me.danielharman.kotlinspringbot.services

import org.springframework.stereotype.Component
import uk.me.danielharman.kotlinspringbot.helpers.Failure
import uk.me.danielharman.kotlinspringbot.helpers.OperationResult
import uk.me.danielharman.kotlinspringbot.helpers.Success
import java.io.IOException
import java.nio.file.Files
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readText

data class ProcessResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
)

/**
 * Runs external processes without a shell, so arguments are never interpreted by one.
 */
@Component
class ProcessRunner {
    fun run(
        command: List<String>,
        timeout: Duration,
    ): OperationResult<ProcessResult, String> {
        // Output goes to files rather than pipes so a chatty process can't block on a full pipe buffer
        val stdout = Files.createTempFile("process-", ".out")
        val stderr = Files.createTempFile("process-", ".err")
        try {
            val process =
                try {
                    ProcessBuilder(command)
                        .redirectOutput(stdout.toFile())
                        .redirectError(stderr.toFile())
                        .start()
                } catch (e: IOException) {
                    return Failure("Could not start ${command.first()}: ${e.message}")
                }
            // Nothing is sent to the process, so close its input rather than leave it waiting on it
            process.outputStream.close()

            val finished =
                try {
                    process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)
                } catch (e: InterruptedException) {
                    destroyWithChildren(process)
                    throw e
                }

            if (!finished) {
                destroyWithChildren(process)
                return Failure("${command.first()} did not finish within ${timeout.toSeconds()} seconds")
            }

            return Success(ProcessResult(process.exitValue(), stdout.readText(), stderr.readText()))
        } finally {
            stdout.deleteIfExists()
            stderr.deleteIfExists()
        }
    }

    // yt-dlp starts its own child processes (such as Deno), which would otherwise outlive it. They are
    // collected before the parent is killed, as they stop being its descendants once it exits.
    private fun destroyWithChildren(process: Process) {
        process.descendants().forEach { it.destroyForcibly() }
        process.destroyForcibly()
    }
}
