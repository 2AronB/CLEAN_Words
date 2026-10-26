package dev.clean.words

import android.os.Looper
import android.os.SystemClock
import androidx.lifecycle.ViewModelStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.clean.words.core.*
import dev.clean.words.games.bee.BeeGame
import dev.clean.words.games.wordish.WordishGame
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class GenerationSessionAndroidTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private fun ui(block: () -> Unit) = instrumentation.runOnMainSync(block)
    private fun eventually(label: String, condition: () -> Boolean) {
        repeat(200) { if (condition()) return; SystemClock.sleep(50) }
        assertTrue("Timed out: $label", condition())
    }
    private fun freshDirectory() = File(context.cacheDir, "generation-test-${UUID.randomUUID()}").also { assertTrue(it.mkdirs()) }
    private fun asset(name: String): String {
        assertNotEquals("Asset preparation must stay off main", Looper.getMainLooper(), Looper.myLooper())
        return context.assets.open(name).bufferedReader().use { it.readText() }
    }
    private fun session(directory: File, source: (String) -> String = ::asset): Pair<AppSession, ViewModelStore> {
        lateinit var value: AppSession
        val store = ViewModelStore()
        ui { value = AppSession(directory, source); store.put("session", value) }
        return value to store
    }
    private fun waitStarted(session: AppSession): String {
        var id: String? = null
        eventually("durable generated start") { ui { id = session.openedAttemptId }; id != null }
        return id!!
    }

    @Test fun freshOfflineDailyExtraDurableStartAndRestartUseFrozenSnapshots() {
        val directory = freshDirectory()
        val (s, owner) = session(directory)
        var expected = Library()
        try {
            listOf(WordishGame, BeeGame).forEach { game ->
                ui { s.open(game, PuzzleMode.DAILY); s.open(game, PuzzleMode.DAILY) }
                val dailyId = waitStarted(s)
                val dailyOnDisk = AtomicLibraryStore(directory).load().library!!.attempts.single { it.id == dailyId }
                assertNotNull(dailyOnDisk.puzzle.data["generator"])
                ui { assertEquals(dailyOnDisk, s.library.attempts.single { it.id == dailyId }); s.consumeOpenedAttempt() }
                repeat(3) {
                    ui { s.open(game, PuzzleMode.EXTRA); s.open(game, PuzzleMode.EXTRA) }
                    val extra = waitStarted(s)
                    assertNotNull(AtomicLibraryStore(directory).load().library!!.attempts.first { it.id == extra }.puzzle.data["generator"])
                    ui { s.consumeOpenedAttempt() }
                }
                ui {
                    val relevant = s.library.attempts.filter { it.gameId == game.id }
                    val adapter = OfflinePuzzleGeneration(::asset)
                    assertEquals(4, relevant.size)
                    assertEquals(4, relevant.map { adapter.signature(game.id, it.puzzle) }.distinct().size)
                    s.open(game, PuzzleMode.DAILY)
                    assertEquals(dailyId, s.openedAttemptId)
                    assertEquals(dailyOnDisk.puzzle, s.library.attempts.first { it.id == dailyId }.puzzle)
                    s.consumeOpenedAttempt()
                }
            }
            ui { expected = s.library }
            assertEquals(8, expected.attempts.size)
            assertEquals(expected, AtomicLibraryStore(directory).load().library)
        } finally { ui { owner.clear() } }
        val (restored, nextOwner) = session(directory) { throw AssertionError("Resume must not prepare source or regenerate") }
        try {
            ui {
                assertEquals(expected, restored.library)
                listOf(WordishGame, BeeGame).forEach { game ->
                    restored.open(game, PuzzleMode.DAILY)
                    assertEquals(expected.attempts.single { it.gameId == game.id && it.mode == PuzzleMode.DAILY }.id, restored.openedAttemptId)
                    restored.consumeOpenedAttempt()
                }
            }
        } finally { ui { nextOwner.clear() } }
    }

    @Test fun cancelRolloverAndSourceFailureLeaveNoAttemptAndCanRetry() {
        val directory = freshDirectory()
        val entered = CountDownLatch(1); val release = CountDownLatch(1)
        val reads = AtomicInteger(); val corrupt = AtomicBoolean(false)
        val (s, owner) = session(directory) { name ->
            reads.incrementAndGet(); entered.countDown()
            assertTrue(release.await(8, TimeUnit.SECONDS))
            if (corrupt.get()) "bad index" else asset(name)
        }
        try {
            ui { s.open(BeeGame, PuzzleMode.DAILY); s.open(BeeGame, PuzzleMode.DAILY) }
            assertTrue(entered.await(5, TimeUnit.SECONDS)); assertEquals(1, reads.get())
            ui {
                val tomorrow = AttemptRules.puzzleDate(Instant.now(), ZoneId.systemDefault()).plusDays(1)
                s.checkBoundary(tomorrow)
                assertNull(s.launching); assertTrue(s.library.attempts.isEmpty())
            }
            release.countDown(); SystemClock.sleep(200)
            ui { assertNull(s.openedAttemptId); assertTrue(s.library.attempts.isEmpty()) }
            corrupt.set(true)
            ui { s.open(BeeGame, PuzzleMode.EXTRA) }
            eventually("visible source error") { var error = false; ui { error = s.launchError != null }; error }
            ui { assertTrue(s.library.attempts.isEmpty()); assertFalse(s.pendingSave) }
            assertTrue(AtomicLibraryStore(directory).load().library!!.attempts.isEmpty())
            corrupt.set(false)
            ui { s.retryGeneration(BeeGame) }
            waitStarted(s)
            ui { assertEquals(1, s.library.attempts.size); assertNull(s.launchError) }
        } finally { release.countDown(); ui { owner.clear() } }
    }

    @Test fun failedStartSavePreservesLegacyHistoryAndRetryDoesNotStartFailedPuzzle() {
        val directory = freshDirectory(); val disk = AtomicLibraryStore(directory)
        val now = Instant.now().toString()
        val legacy = Attempt("legacy", "bee", PuzzleMode.EXTRA, null, BeeGame.puzzles.first(),
            state = mapOf("entry" to "CENT"), startedAt = now, updatedAt = now)
        val before = Library(listOf(legacy)); disk.save(before)
        val collision = File(directory, "attempts.pending.cw"); assertTrue(collision.mkdir())
        val (s, owner) = session(directory)
        try {
            ui { s.open(WordishGame, PuzzleMode.EXTRA) }
            eventually("visible start-save failure") { var failed = false; ui { failed = s.pendingSave }; failed }
            ui { assertNull(s.openedAttemptId); assertEquals(before, s.library); assertNotNull(s.error) }
            assertEquals(before, disk.load().library)
            assertTrue(collision.delete()) // only this test's empty collision directory; no player data
            ui { s.retrySave() }
            eventually("retry save") { var pending = true; ui { pending = s.pendingSave }; !pending }
            assertEquals(before, disk.load().library)
            ui { s.open(WordishGame, PuzzleMode.EXTRA) }
            waitStarted(s)
            ui { assertEquals(2, s.library.attempts.size); assertEquals(legacy, s.library.attempts.first()) }
        } finally { ui { owner.clear() } }
    }
}
