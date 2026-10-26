package dev.clean.words.core

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.Executor

class GenerationCoordinatorTest {
    private class Lane : Executor {
        val tasks = ArrayDeque<Runnable>()
        override fun execute(command: Runnable) { tasks.addLast(command) }
        fun run() { while (tasks.isNotEmpty()) tasks.removeFirst().run() }
    }
    private val day = LocalDate.of(2026, 10, 25)
    private fun request(mode: PuzzleMode = PuzzleMode.DAILY, id: String = "a") =
        GenerationRequest("wordish", mode, day, 42, id, "policy-v1")
    private val puzzle = PuzzleRecord("generated", title = "Fixture", data = mapOf("answer" to "APPLE"))

    @Test fun seedIdentityPinsDateGamePolicyAndExtraAttempt() {
        val a = GenerationSeeds.daily("wordish", "policy-v1", day)
        assertEquals(a, GenerationSeeds.daily("wordish", "policy-v1", day))
        assertNotEquals(a, GenerationSeeds.daily("bee", "policy-v1", day))
        assertNotEquals(a, GenerationSeeds.daily("wordish", "policy-v2", day))
        assertNotEquals(a, GenerationSeeds.daily("wordish", "policy-v1", day.plusDays(1)))
        assertNotEquals(a, GenerationSeeds.extra("wordish", "policy-v1", "one"))
        assertNotEquals(GenerationSeeds.extra("wordish", "policy-v1", "one"), GenerationSeeds.extra("wordish", "policy-v1", "two"))
        val zone = ZoneId.of("America/Indiana/Indianapolis")
        assertNotEquals(
            GenerationSeeds.daily("wordish", "v1", AttemptRules.puzzleDate(Instant.parse("2026-10-25T09:59:59Z"), zone)),
            GenerationSeeds.daily("wordish", "v1", AttemptRules.puzzleDate(Instant.parse("2026-10-25T10:00:00Z"), zone)))
    }

    @Test fun doubleTapsAreCoalescedAndResultsWaitForUiLane() {
        val work = Lane(); val ui = Lane(); var calls = 0
        val results = mutableListOf<GenerationOutcome>()
        val c = GenerationCoordinator(work, ui, PuzzleGenerator { _, _ -> calls++; GenerationOutcome.Ready(puzzle) })
        c.start(request(), results::add); c.start(request(id = "double-tap"), results::add)
        assertEquals(1, work.tasks.size); assertEquals(0, calls)
        work.run(); assertTrue(results.isEmpty()); ui.run()
        assertEquals(1, calls); assertEquals(listOf(GenerationOutcome.Ready(puzzle)), results)
        c.start(request(PuzzleMode.EXTRA), results::add)
        c.start(request(PuzzleMode.EXTRA, "other"), results::add)
        assertEquals(1, work.tasks.size)
    }

    @Test fun navigationCancellationSuppressesAlreadyQueuedCallbacks() {
        val work = Lane(); val ui = Lane(); val results = mutableListOf<GenerationOutcome>()
        val c = GenerationCoordinator(work, ui, PuzzleGenerator { _, _ -> GenerationOutcome.Ready(puzzle) })
        c.start(request(), results::add); work.run(); c.cancel(); ui.run()
        assertTrue(results.isEmpty())
        c.start(request(), results::add); c.cancel(); work.run(); ui.run()
        assertTrue(results.isEmpty())
    }

    @Test fun cancellationIsVisibleInsideLongWorkAndNewRequestWins() {
        val work = Lane(); val ui = Lane(); val ids = mutableListOf<String>()
        lateinit var c: GenerationCoordinator
        c = GenerationCoordinator(work, ui, PuzzleGenerator { req, cancelled ->
            if (req.attemptId == "a") { c.cancel(); assertTrue(cancelled()); GenerationOutcome.Cancelled }
            else GenerationOutcome.Ready(puzzle.copy(id = req.attemptId))
        })
        c.start(request()) { ids += "old" }; work.run(); ui.run()
        assertTrue(ids.isEmpty())
        c.start(request(id = "b")) { ids += (it as GenerationOutcome.Ready).puzzle.id }
        c.start(request().copy(date = day.plusDays(1), attemptId = "c")) { ids += (it as GenerationOutcome.Ready).puzzle.id }
        work.run(); ui.run(); assertEquals(listOf("c"), ids)
    }

    @Test fun sourceFailureAndExhaustionCanRetryWithoutCreatingHistory() {
        val work = Lane(); val ui = Lane(); var fail = true
        val results = mutableListOf<GenerationOutcome>()
        val c = GenerationCoordinator(work, ui, PuzzleGenerator { _, _ ->
            if (fail) throw IllegalArgumentException("index mismatch") else GenerationOutcome.Unavailable("Exhausted")
        })
        c.start(request(), results::add); work.run(); ui.run()
        assertTrue((results.single() as GenerationOutcome.Unavailable).message.contains("index mismatch"))
        fail = false
        c.start(request(), results::add); work.run(); ui.run()
        assertEquals(GenerationOutcome.Unavailable("Exhausted"), results.last())
        assertEquals(Library(), LibraryCodec.decode(LibraryCodec.encode(Library())))
    }
}
