package dev.clean.words

import android.os.Looper
import android.os.SystemClock
import androidx.lifecycle.ViewModelStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.clean.words.core.*
import dev.clean.words.games.bee.BeeGame
import dev.clean.words.games.wordish.WordishGame
import dev.clean.words.games.crossword.CrosswordGame
import dev.clean.words.games.crossword.CrosswordRules
import dev.clean.words.games.crossword.CrosswordInput
import dev.clean.words.games.connections.ConnectionsGame
import dev.clean.words.games.connections.ConnectionsRules
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
            listOf(WordishGame, BeeGame, ConnectionsGame, CrosswordGame).forEach { game ->
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
            assertEquals(16, expected.attempts.size)
            assertEquals(expected, AtomicLibraryStore(directory).load().library)
        } finally { ui { owner.clear() } }
        val (restored, nextOwner) = session(directory) { throw AssertionError("Resume must not prepare source or regenerate") }
        try {
            ui {
                assertEquals(expected, restored.library)
                listOf(WordishGame, BeeGame, ConnectionsGame, CrosswordGame).forEach { game ->
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

    @Test fun legacyConnectionsDailyWinsAndFailedGeneratedStartCannotReplaceIt() {
        val directory = freshDirectory(); val disk = AtomicLibraryStore(directory)
        val now = Instant.now(); val zone = ZoneId.systemDefault()
        val old = AttemptRules.create(Library(), ConnectionsGame, PuzzleMode.DAILY, now, zone)
        val before = Library(listOf(old)); disk.save(before)
        val collision = File(directory, "attempts.pending.cw"); assertTrue(collision.mkdir())
        val (s, owner) = session(directory)
        try {
            ui {
                s.open(ConnectionsGame, PuzzleMode.DAILY, now, zone)
                assertEquals(old.id, s.openedAttemptId)
                assertEquals(before, s.library)
                assertNull(s.launching)
                s.consumeOpenedAttempt()
                s.open(ConnectionsGame, PuzzleMode.EXTRA)
            }
            eventually("Connections save failure") { var blocked = false; ui { blocked = s.pendingSave }; blocked }
            ui { assertNull(s.openedAttemptId); assertEquals(before, s.library) }
            assertEquals(before, disk.load().library)
            assertTrue(collision.delete()) // this test's empty collision only
            ui { s.retrySave() }
            eventually("Connections retry rollback") { var blocked = true; ui { blocked = s.pendingSave }; !blocked }
            assertEquals(before, disk.load().library)
            ui { s.open(ConnectionsGame, PuzzleMode.EXTRA) }
            val id = waitStarted(s)
            val generated = disk.load().library!!.attempts.single { it.id == id }
            assertNotNull(generated.puzzle.data["generator"])
            assertEquals(before.attempts.first(), disk.load().library!!.attempts.first())
            var state = emptyMap<String, String>()
            for (tile in 0..3) state = ConnectionsRules.select(state, tile)
            state = ConnectionsRules.submit(generated.puzzle, state)
            ui { s.change(id, state, ConnectionsGame) }
            eventually("Connections group retained on disk") { disk.load().library!!.attempts.single { it.id == id }.state == state }
        } finally { ui { owner.clear() } }
        val (restored, nextOwner) = session(directory)
        try {
            ui {
                assertEquals(old, restored.library.attempts.first())
                assertEquals(setOf(0), ConnectionsRules.solved(restored.library.attempts.last().state))
                restored.open(ConnectionsGame, PuzzleMode.DAILY, now, zone)
                assertEquals(old.id, restored.openedAttemptId)
            }
        } finally { ui { nextOwner.clear() } }
    }

    @Test fun legacyCrosswordDailyAndCompletedHistorySurviveLargerGeneratedPlayAndRestart() {
        val directory = freshDirectory(); val disk = AtomicLibraryStore(directory)
        val now = Instant.now(); val zone = ZoneId.systemDefault()
        val starter = AttemptRules.create(Library(), CrosswordGame, PuzzleMode.DAILY, now, zone)
        val starterBoard = requireNotNull(CrosswordRules.board(starter.puzzle))
        val old = AttemptRules.update(starter, CrosswordInput.type(starterBoard, emptyMap(), "AX"), CrosswordGame, now)
        val historyPuzzle = CrosswordGame.puzzles[1]
        val history = AttemptRules.createSnapshot(Library(listOf(old)), CrosswordGame, PuzzleMode.EXTRA,
            historyPuzzle, now.minusSeconds(3600), zone).let { attempt ->
            AttemptRules.update(attempt, mapOf("cells" to requireNotNull(CrosswordRules.board(historyPuzzle)).grid),
                CrosswordGame, now.minusSeconds(3500))
        }
        assertEquals(PlayStatus.COMPLETED, history.result.status)
        val before = Library(listOf(old, history)); disk.save(before)
        val (s, owner) = session(directory)
        var expected = before
        var generatedId = ""
        try {
            ui {
                s.open(CrosswordGame, PuzzleMode.DAILY, now, zone)
                assertEquals(old.id, s.openedAttemptId)
                assertEquals(before, s.library)
                assertNull(s.launching)
                s.consumeOpenedAttempt()
                s.open(CrosswordGame, PuzzleMode.EXTRA)
            }
            generatedId = waitStarted(s)
            val generated = disk.load().library!!.attempts.single { it.id == generatedId }
            assertNotNull(generated.puzzle.data["generator"])
            val board = requireNotNull(CrosswordRules.board(generated.puzzle))
            assertEquals(7, board.width); assertEquals(7, board.height)
            assertEquals(emptyList<String>(), CrosswordRules.validate(generated.puzzle))
            val highIndex = board.grid.indices.last { it > 24 && board.grid[it] != '#' }
            val wrong = if (board.grid[highIndex] == 'Z') "X" else "Z"
            val containing = board.entries().filter { highIndex in it.cells }
            assertTrue(containing.isNotEmpty())
            var state = CrosswordInput.select(board, emptyMap(), highIndex, containing.first().direction)
            state = CrosswordRules.enter(board, state, highIndex, wrong)
            state = CrosswordRules.check(board, state)
            assertEquals(setOf(highIndex), CrosswordRules.checked(state))
            // Crossing edits share one saved cell, regardless of the generated template's layout.
            val crossing = board.entries().flatMap { it.cells }.groupingBy { it }.eachCount()
                .entries.first { it.value == 2 && it.key != highIndex }.key
            state = CrosswordRules.enter(board, state, crossing, board.grid[crossing].toString())
            assertEquals(setOf(highIndex), CrosswordRules.checked(state))
            val finalState = state
            ui { s.change(generatedId, finalState, CrosswordGame) }
            eventually("larger crossword edits and checked cells durable") {
                disk.load().library!!.attempts.single { it.id == generatedId }.state == finalState
            }
            ui { expected = s.library }
            assertEquals(old, expected.attempts[0])
            assertEquals(history, expected.attempts[1])
            assertEquals(generated.puzzle, expected.attempts.last().puzzle)
            assertTrue(expected.attempts.last().result.assisted)
            assertEquals(expected, disk.load().library)
        } finally { ui { owner.clear() } }
        val (restored, nextOwner) = session(directory) { error("Saved crossword must reopen without source loading") }
        try {
            ui {
                assertEquals(expected, restored.library)
                val reopened = restored.library.attempts.single { it.id == generatedId }
                val board = requireNotNull(CrosswordRules.board(reopened.puzzle))
                assertEquals(49, CrosswordRules.cells(board, reopened.state).length)
                assertTrue(CrosswordRules.checked(reopened.state).single() > 24)
                assertEquals(CrosswordRules.result(board, reopened.state), reopened.result)
                restored.open(CrosswordGame, PuzzleMode.DAILY, now, zone)
                assertEquals(old.id, restored.openedAttemptId)
                assertEquals(old, restored.library.attempts.first())
                assertEquals(history, restored.library.attempts[1])
            }
            val reopened = expected.attempts.single { it.id == generatedId }
            val board = requireNotNull(CrosswordRules.board(reopened.puzzle))
            var completedState = reopened.state
            board.grid.indices.filter { board.grid[it] != '#' }.forEach { index ->
                completedState = CrosswordRules.enter(board, completedState, index, board.grid[index].toString())
            }
            assertEquals(PlayStatus.COMPLETED, CrosswordRules.result(board, completedState).status)
            assertTrue(CrosswordRules.checked(completedState).isEmpty())
            ui { restored.change(generatedId, completedState, CrosswordGame) }
            eventually("completed larger crossword result durable") {
                disk.load().library!!.attempts.single { it.id == generatedId }.result.status == PlayStatus.COMPLETED
            }
            val completed = disk.load().library!!.attempts.single { it.id == generatedId }
            assertEquals(reopened.puzzle, completed.puzzle)
            assertEquals(completedState, completed.state)
            assertNotNull(completed.completedAt)
            assertEquals(board.grid.count { it != '#' }, completed.result.score)
            assertTrue(completed.result.assisted)
            ui {
                restored.open(CrosswordGame, PuzzleMode.DAILY, now, zone)
                assertEquals(old.id, restored.openedAttemptId)
                assertEquals(old, restored.library.attempts.first())
                assertEquals(history, restored.library.attempts[1])
                assertEquals(completed, restored.library.attempts.last())
            }
        } finally { ui { nextOwner.clear() } }
    }
}
