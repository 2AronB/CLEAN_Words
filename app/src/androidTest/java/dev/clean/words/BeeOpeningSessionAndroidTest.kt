package dev.clean.words

import android.os.Looper
import android.os.SystemClock
import androidx.lifecycle.ViewModelStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.clean.words.core.*
import dev.clean.words.games.bee.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

/** AppSession and real app-private files; fixtures never touch the player's play-library. */
@RunWith(AndroidJUnit4::class)
class BeeOpeningSessionAndroidTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private fun ui(block: () -> Unit) = instrumentation.runOnMainSync(block)
    private fun eventually(label: String, condition: () -> Boolean) {
        repeat(240) { if (condition()) return; SystemClock.sleep(50) }
        assertTrue("Timed out: $label", condition())
    }
    private fun directory() = File(context.cacheDir, "bee-opening-session-${UUID.randomUUID()}")
        .also { assertTrue(it.mkdirs()) }
    private fun asset(name: String): String {
        assertNotEquals("Generation source loading must stay off main", Looper.getMainLooper(), Looper.myLooper())
        return context.assets.open(name).bufferedReader().use { it.readText() }
    }
    private fun session(directory: File, source: (String) -> String = ::asset): Pair<AppSession, ViewModelStore> {
        val owner = ViewModelStore()
        lateinit var s: AppSession
        ui { s = AppSession(directory, source); owner.put("session", s) }
        return s to owner
    }
    private fun snapshot(s: AppSession): Library {
        lateinit var library: Library
        ui { library = s.library }
        return library
    }
    private fun waitStarted(s: AppSession): String {
        var id: String? = null
        eventually("durable Bee start") { ui { id = s.openedAttemptId }; id != null }
        val result = id!!
        ui { s.consumeOpenedAttempt() }
        return result
    }
    private fun waitSaved(s: AppSession) {
        eventually("ordered save complete") {
            var saved = false
            ui { saved = !s.saving && !s.pendingSave }
            saved
        }
    }
    private fun submit(s: AppSession, id: String, word: String) {
        ui {
            val attempt = s.library.attempts.single { it.id == id }
            val before = BeeState.from(attempt.state)
            val next = BeeRules.submit(BeePuzzle.from(attempt.puzzle), before, word)
                .copy(editRevision = before.editRevision + 1)
            s.change(id, next.toMap(), BeeGame)
        }
    }
    private fun fixtureBoard(): BeePuzzle {
        val base = BeePuzzle.from(BeeGame.puzzles.first())
        val familiar = (listOf("CENTRAL") + base.goalWords.sorted().filter { it != "CENTRAL" }).take(10).toSet()
        return BeeOpening.freeze(base.copy(goalWords = familiar), mapOf(
            "generator" to "android-session-fixture-v3", "profile" to "fixture-v3",
            "profileThresholds" to "fixture-thresholds-v3", "sourceIndex" to "frozen-fixture",
            "sourceIndexSha256" to "frozen-fixture-hash", "familiarityVersion" to "fixture-ai-editorial-v1",
        ), BeeOpeningQuality(0, OfflinePuzzleGeneration.beeOpeningApproval))
    }
    private fun fixtureAttempt(board: BeePuzzle, mode: PuzzleMode = PuzzleMode.EXTRA, yesterday: Boolean = false): Attempt {
        val now = Instant.now().minusSeconds(if (yesterday) 86400 else 0)
        return AttemptRules.createSnapshot(Library(), BeeGame, mode, board.snapshot("android-opening-fixture"),
            now, ZoneId.systemDefault(), "fixture-${UUID.randomUUID()}")
    }
    private fun noSource(@Suppress("UNUSED_PARAMETER") name: String): String =
        throw AssertionError("Saved play must resume without reading source data")

    @Test fun realDailyExtraOpeningPersistsOrderFreezesScoreAndLegacyDailyStillWins() {
        val folder = directory(); val disk = AtomicLibraryStore(folder)
        val (s, owner) = session(folder)
        lateinit var daily: Attempt
        lateinit var saved: Library
        try {
            ui { s.open(BeeGame, PuzzleMode.DAILY) }
            val dailyId = waitStarted(s)
            daily = snapshot(s).attempts.single { it.id == dailyId }
            assertEquals(BeeOpening.SCORING, daily.puzzle.data["scoringVersion"])
            assertEquals("0", daily.puzzle.data["minimumFamiliarRatioPermille"])
            assertEquals(OfflinePuzzleGeneration.beeOpeningApproval, daily.puzzle.data["qualityApprovalVersion"])
            val board = BeePuzzle.from(daily.puzzle)
            val order = board.validWords.sortedDescending().take(11)
            order.take(10).forEach { submit(s, dailyId, it) }
            waitSaved(s)
            val opening = AttemptPresentation.inspect(snapshot(s).attempts.single { it.id == dailyId }).opening!!
            assertTrue(opening.recorded); assertEquals(order.take(10), opening.words)
            submit(s, dailyId, order.last()); waitSaved(s)
            val continued = AttemptPresentation.inspect(snapshot(s).attempts.single { it.id == dailyId }).opening!!
            assertEquals(opening.points, continued.points); assertEquals(opening.percent, continued.percent)
            assertEquals(11, continued.foundCount); assertTrue(continued.allWordPoints > opening.allWordPoints)
            ui { s.open(BeeGame, PuzzleMode.EXTRA) }
            val extraId = waitStarted(s)
            val extra = snapshot(s).attempts.single { it.id == extraId }
            assertEquals(BeeOpening.SCORING, extra.puzzle.data["scoringVersion"])
            assertNotEquals(daily.puzzle.data["contentSignature"], extra.puzzle.data["contentSignature"])
            saved = snapshot(s)
            assertEquals(saved, disk.load().library)
        } finally { ui { owner.clear() } }

        val (restored, nextOwner) = session(folder, ::noSource)
        try {
            ui { restored.open(BeeGame, PuzzleMode.DAILY) }
            assertEquals(daily.id, waitStarted(restored))
            assertEquals(saved, snapshot(restored))
            assertEquals(11, BeeState.from(snapshot(restored).attempts.first { it.id == daily.id }.state).acceptedOrder!!.size)
        } finally { ui { nextOwner.clear() } }

        val legacyFolder = directory(); val legacyDisk = AtomicLibraryStore(legacyFolder)
        val legacy = AttemptRules.createSnapshot(Library(), BeeGame, PuzzleMode.DAILY,
            BeeGame.puzzles.first(), Instant.now(), ZoneId.systemDefault(), "legacy-daily")
        legacyDisk.save(Library(listOf(legacy)))
        val (old, oldOwner) = session(legacyFolder, ::noSource)
        try {
            ui { old.open(BeeGame, PuzzleMode.DAILY) }
            assertEquals(legacy.id, waitStarted(old))
            assertEquals(legacy, snapshot(old).attempts.single())
            assertEquals(Library(listOf(legacy)), legacyDisk.load().library)
        } finally { ui { oldOwner.clear() } }
    }

    @Test fun goalBeforeTenRemainsResumableAcrossRestartUntilOpeningIsRecorded() {
        val folder = directory(); val disk = AtomicLibraryStore(folder)
        val board = fixtureBoard(); assertEquals(7, board.wordTarget)
        val before = fixtureAttempt(board, PuzzleMode.DAILY, yesterday = true)
        disk.save(Library(listOf(before)))
        val order = board.validWords.sortedDescending().take(10)
        val (s, owner) = session(folder, ::noSource)
        lateinit var goalReached: Attempt
        try {
            order.take(7).forEach { submit(s, before.id, it) }; waitSaved(s)
            goalReached = snapshot(s).attempts.single()
            assertEquals(PlayStatus.COMPLETED, goalReached.result.status)
            assertNotNull(goalReached.completedAt)
            assertFalse(AttemptPresentation.inspect(goalReached).opening!!.recorded)
            assertTrue(AttemptPresentation.inspect(goalReached).needsResume)
            assertEquals("…", AttemptPresentation.inspect(goalReached).calendarMark)
            assertEquals(goalReached, AttemptRules.previousUnfinished(snapshot(s), "bee",
                AttemptRules.puzzleDate(Instant.now(), ZoneId.systemDefault())))
        } finally { ui { owner.clear() } }
        val (restored, nextOwner) = session(folder, ::noSource)
        try {
            assertEquals(goalReached, snapshot(restored).attempts.single())
            order.drop(7).forEach { submit(restored, before.id, it) }; waitSaved(restored)
            val finished = snapshot(restored).attempts.single()
            val presentation = AttemptPresentation.inspect(finished)
            val opening = presentation.opening!!
            assertTrue(opening.recorded); assertTrue(opening.goalReached)
            assertFalse(presentation.needsResume); assertEquals("✓", presentation.calendarMark)
            assertEquals(goalReached.completedAt, finished.completedAt)
            assertEquals(before.puzzleDate, finished.puzzleDate)
            assertNull(AttemptRules.previousUnfinished(snapshot(restored), "bee",
                AttemptRules.puzzleDate(Instant.now(), ZoneId.systemDefault())))
            assertEquals(snapshot(restored), disk.load().library)
        } finally { ui { nextOwner.clear() } }
    }

    @Test fun failedSaveRetryKeepsLatestOrderedOpeningAndAllowsContinuedPlay() {
        val folder = directory(); val disk = AtomicLibraryStore(folder)
        val board = fixtureBoard(); val order = board.validWords.sortedDescending().take(11)
        val initial = fixtureAttempt(board)
        var state = BeeState()
        order.take(8).forEach { state = BeeRules.submit(board, state, it).copy(editRevision = state.editRevision + 1) }
        val baseline = AttemptRules.update(initial, state.toMap(), BeeGame, Instant.now())
        disk.save(Library(listOf(baseline)))
        val collision = File(folder, "attempts.pending.cw"); assertTrue(collision.mkdir())
        val (s, owner) = session(folder, ::noSource)
        try {
            // Both edits happen in one UI turn, before the failed-write acknowledgement returns.
            ui {
                var live = BeeState.from(s.library.attempts.single().state)
                order.subList(8, 10).forEach { word ->
                    live = BeeRules.submit(board, live, word).copy(editRevision = live.editRevision + 1)
                    s.change(initial.id, live.toMap(), BeeGame)
                }
            }
            eventually("visible failed ordered save") { var failed = false; ui { failed = s.pendingSave }; failed }
            val latest = snapshot(s).attempts.single()
            val opening = AttemptPresentation.inspect(latest).opening!!
            assertEquals(order.take(10), opening.words); assertTrue(opening.recorded)
            assertEquals(Library(listOf(baseline)), disk.load().library)
            assertTrue(collision.delete()) // isolated empty test collision only, never player storage
            ui { s.retrySave() }; waitSaved(s)
            assertEquals(Library(listOf(latest)), disk.load().library)
            submit(s, initial.id, order[10]); waitSaved(s)
            val continued = AttemptPresentation.inspect(snapshot(s).attempts.single()).opening!!
            assertEquals(opening.points, continued.points); assertEquals(opening.percent, continued.percent)
            assertEquals(11, continued.foundCount)
            assertEquals(snapshot(s), disk.load().library)
        } finally { ui { owner.clear() } }
        val (restored, nextOwner) = session(folder, ::noSource)
        try {
            val progress = AttemptPresentation.inspect(snapshot(restored).attempts.single()).opening!!
            assertTrue(progress.recorded); assertEquals(order.take(10), progress.words); assertEquals(11, progress.foundCount)
        } finally { ui { nextOwner.clear() } }
    }

    @Test fun malformedSavedOrderCannotBeOverwrittenByAValidLookingRepair() {
        val folder = directory(); val disk = AtomicLibraryStore(folder)
        val board = fixtureBoard(); val initial = fixtureAttempt(board)
        val words = board.validWords.sortedDescending().take(10)
        var state = BeeState()
        words.forEach { state = BeeRules.submit(board, state, it).copy(editRevision = state.editRevision + 1) }
        val valid = AttemptRules.update(initial, state.toMap(), BeeGame, Instant.now())
        val malformed = valid.copy(state = valid.state + ("acceptedOrder" to "${words.first()},${words.first()}"))
        val original = Library(listOf(malformed)); disk.save(original)
        val bytes = File(folder, "attempts.cw").readBytes()
        val (s, owner) = session(folder, ::noSource)
        try {
            assertTrue(AttemptPresentation.inspect(snapshot(s).attempts.single()).errors.isNotEmpty())
            ui { s.change(initial.id, state.copy(editRevision = 100).toMap(), BeeGame) }
            assertEquals(original, snapshot(s)); assertEquals(original, disk.load().library)
            ui { assertNotNull(s.notice); assertFalse(s.pendingSave) }
            assertArrayEquals(bytes, File(folder, "attempts.cw").readBytes())
        } finally { ui { owner.clear() } }
        val (restored, nextOwner) = session(folder, ::noSource)
        try {
            assertEquals(original, snapshot(restored))
            assertEquals(malformed.state, snapshot(restored).attempts.single().state)
            assertEquals(valid.result, snapshot(restored).attempts.single().result)
        } finally { ui { nextOwner.clear() } }
    }
}
