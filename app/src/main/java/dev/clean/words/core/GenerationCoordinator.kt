package dev.clean.words.core

import java.security.MessageDigest
import java.time.LocalDate
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

data class GenerationRequest(
    val gameId: String,
    val mode: PuzzleMode,
    val date: LocalDate,
    val seed: Long,
    val attemptId: String,
    val policy: String,
    val recentSignatures: Set<String> = emptySet(),
    val starterOrdinal: Int = 0,
)

sealed interface GenerationOutcome {
    data class Ready(val puzzle: PuzzleRecord) : GenerationOutcome
    data class Unavailable(val message: String) : GenerationOutcome
    data object Cancelled : GenerationOutcome
}

fun interface PuzzleGenerator {
    fun generate(request: GenerationRequest, cancelled: () -> Boolean): GenerationOutcome
}

object GenerationSeeds {
    /** A pinned UTF-8/SHA-256 identity; independent of library order, extras and JVM hashCode. */
    fun daily(gameId: String, policy: String, date: LocalDate): Long =
        seed("clean-words-daily-v1|$gameId|$policy|$date")
    fun extra(gameId: String, policy: String, attemptId: String): Long =
        seed("clean-words-extra-v1|$gameId|$policy|$attemptId")
    private fun seed(value: String): Long {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return java.nio.ByteBuffer.wrap(bytes).long
    }
}

/** One active UI request. Cooperative tokens also suppress already-posted late callbacks. */
class GenerationCoordinator(
    private val worker: Executor,
    private val callbacks: Executor,
    private val generator: PuzzleGenerator,
) {
    private data class Job(val request: GenerationRequest, val cancelled: AtomicBoolean = AtomicBoolean())
    private var active: Job? = null // start/cancel/callback dispatch are on the caller's serial UI lane

    fun start(request: GenerationRequest, result: (GenerationOutcome) -> Unit) {
        val previous = active
        if (previous != null && previous.request.gameId == request.gameId &&
            previous.request.mode == request.mode &&
            (request.mode == PuzzleMode.EXTRA || previous.request.date == request.date)) return
        cancel()
        val job = Job(request)
        active = job
        worker.execute {
            val outcome = try {
                if (job.cancelled.get()) GenerationOutcome.Cancelled
                else generator.generate(request, job.cancelled::get)
            } catch (e: Exception) {
                GenerationOutcome.Unavailable("Local puzzle source could not be prepared: ${e.message}. Saved puzzles are preserved. Retry to try again.")
            }
            callbacks.execute {
                if (active === job && !job.cancelled.get()) {
                    active = null
                    result(outcome)
                }
            }
        }
    }

    fun cancel() { active?.cancelled?.set(true); active = null }
}
