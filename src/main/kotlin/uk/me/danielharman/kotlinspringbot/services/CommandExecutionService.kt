package uk.me.danielharman.kotlinspringbot.services

import jakarta.annotation.PreDestroy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Runs command handlers off JDA's event thread so that slow commands (blocking REST calls,
 * downloads, history scans) don't stall every other event the bot receives.
 */
@Service
class CommandExecutionService {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val executor: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()

    private val scheduler: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().name("command-scheduler").factory())

    /**
     * Runs [task] asynchronously. Exceptions are logged and passed to [onError] so the caller can
     * tell the user something went wrong.
     */
    fun execute(
        description: String,
        onError: (Exception) -> Unit = {},
        task: () -> Unit,
    ) {
        executor.execute {
            try {
                task()
            } catch (e: Exception) {
                logger.error("Error while executing $description", e)
                try {
                    onError(e)
                } catch (reportError: Exception) {
                    logger.error("Failed to report error for $description", reportError)
                }
            }
        }
    }

    fun schedule(
        delayMillis: Long,
        task: () -> Unit,
    ) {
        scheduler.schedule(
            {
                try {
                    task()
                } catch (e: Exception) {
                    logger.error("Error while running scheduled command task", e)
                }
            },
            delayMillis,
            TimeUnit.MILLISECONDS,
        )
    }

    @PreDestroy
    fun shutdown() {
        scheduler.shutdownNow()
        executor.shutdown()
    }
}
