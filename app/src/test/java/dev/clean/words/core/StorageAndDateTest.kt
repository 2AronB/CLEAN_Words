package dev.clean.words.core

import androidx.compose.runtime.Composable
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.nio.ByteBuffer

class StorageAndDateTest {
    @get:Rule val temporary = TemporaryFolder()
    private val zone = ZoneId.of("America/Indiana/Indianapolis")
    private val game = object : GameModule {
        override val id = "fixture"
        override val title = "Fixture"
        override val description = "Test fixture"
        override val puzzles = (1..10).map { PuzzleRecord("p$it", title = "Puzzle $it", data = mapOf("answer" to "A", "unicode" to "é 🐝\n\\")) }
        override fun result(puzzle: PuzzleRecord, state: Map<String, String>) =
            GameResult(if (state["answer"] == "A") PlayStatus.COMPLETED else PlayStatus.IN_PROGRESS, state["answer"] ?: "empty")
        override fun validate(puzzle: PuzzleRecord) = emptyList<String>()
        @Composable override fun Play(puzzle: PuzzleRecord, state: Map<String,String>, enabled: Boolean, onChange:(Map<String,String>)->Unit) {}
    }
    private fun daily(now: String = "2026-10-25T09:59:59Z") = AttemptRules.create(Library(), game, PuzzleMode.DAILY, Instant.parse(now), zone)

    @Test fun sixAmRolloverAndDstUseLocalCalendarNotElapsedHours() {
        assertEquals(LocalDate.of(2026,10,24), AttemptRules.puzzleDate(Instant.parse("2026-10-25T09:59:59Z"), zone))
        assertEquals(LocalDate.of(2026,10,25), AttemptRules.puzzleDate(Instant.parse("2026-10-25T10:00:00Z"), zone))
        assertEquals(LocalDate.of(2026,10,31), AttemptRules.puzzleDate(Instant.parse("2026-11-01T10:59:59Z"), zone))
        assertEquals(LocalDate.of(2026,11,1), AttemptRules.puzzleDate(Instant.parse("2026-11-01T11:00:00Z"), zone))
    }
    @Test fun dailyStableAdditionalIndependentAndOldSnapshotPreserved() {
        val old = daily()
        val saved = AttemptRules.upsert(Library(), old)
        assertEquals(old, AttemptRules.create(saved, game, PuzzleMode.DAILY, Instant.parse("2026-10-25T09:00:00Z"), zone))
        val next = AttemptRules.create(saved, game, PuzzleMode.DAILY, Instant.parse("2026-10-25T10:00:00Z"), zone)
        val extra = AttemptRules.create(saved, game, PuzzleMode.EXTRA, Instant.parse("2026-10-25T10:00:00Z"), zone)
        assertNotEquals(old.id, next.id); assertNotEquals(next.id, extra.id)
        assertEquals(old, AttemptRules.upsert(saved, extra).attempts.first())
        assertEquals(2, AttemptRules.upsert(saved, next).attempts.size)
    }
    @Test fun lateCompletionKeepsDateAndFirstFinishTimestampAndNoDuplicateResult() {
        val old = daily()
        val finish = Instant.parse("2026-10-26T12:00:00Z")
        val solved = AttemptRules.update(old, mapOf("answer" to "A"), game, finish)
        val again = AttemptRules.update(solved, mapOf("answer" to "A", "bonus" to "yes"), game, finish.plusSeconds(5))
        assertEquals("2026-10-24", solved.puzzleDate)
        assertEquals(finish.toString(), again.completedAt)
        assertEquals(1, AttemptRules.upsert(AttemptRules.upsert(Library(), solved), again).attempts.size)
    }
    @Test fun codecRoundTripsUnicodeContentStateResultAndSnapshots() {
        val a = AttemptRules.update(daily(), mapOf("draft" to "É\n\\🐝", "answer" to "A"), game, Instant.parse("2026-10-27T12:00:00Z"))
        val library = Library(listOf(a.copy(result = a.result.copy(score = 0, maxScore = 7, assisted = true))))
        assertEquals(library, LibraryCodec.decode(LibraryCodec.encode(library)))
    }
    @Test fun corruptTruncatedAndUnsupportedFilesAreRejected() {
        val bytes = LibraryCodec.encode(Library(listOf(daily())))
        assertThrows(IllegalArgumentException::class.java) { LibraryCodec.decode(bytes.copyOf(bytes.size - 1)) }
        bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte()
        assertThrows(IllegalArgumentException::class.java) { LibraryCodec.decode(bytes) }
        val future = LibraryCodec.encode(Library())
        ByteBuffer.wrap(future).putInt(4, 99)
        assertThrows(UnsupportedSaveVersion::class.java) { LibraryCodec.decode(future) }
    }
    @Test fun atomicStoreRoundTripAndLastGoodRecoveryPreserveDamagedFile() {
        val folder = temporary.newFolder()
        val store = AtomicLibraryStore(folder)
        val first = Library(listOf(daily()))
        val second = AttemptRules.upsert(first, AttemptRules.create(first, game, PuzzleMode.EXTRA, Instant.now(), zone))
        store.save(first); store.save(second)
        assertEquals(second, store.load().library)
        val current = java.io.File(folder, "attempts.cw")
        current.writeText("broken")
        val recovered = store.load()
        assertEquals(first, recovered.library); assertFalse(recovered.blocked); assertNotNull(recovered.notice)
        store.save(first)
        assertTrue(folder.listFiles()!!.any { it.name.startsWith("attempts.damaged-") && it.readText() == "broken" })
        assertEquals(first, store.load().library)
    }
    @Test fun futureVersionCannotFallBackToOlderBackupOrBeOverwritten() {
        val folder = temporary.newFolder(); val store = AtomicLibraryStore(folder)
        store.save(Library()); store.save(Library(listOf(daily())))
        val file = java.io.File(folder, "attempts.cw")
        val future = file.readBytes(); ByteBuffer.wrap(future).putInt(4,99); file.writeBytes(future)
        assertTrue(store.load().blocked)
        assertThrows(UnsupportedSaveVersion::class.java) { store.save(Library()) }
        assertArrayEquals(future, file.readBytes())
    }
    @Test fun twoUnreadableCopiesBlockInsteadOfStartingBlank() {
        val folder = temporary.newFolder(); val store = AtomicLibraryStore(folder)
        java.io.File(folder, "attempts.cw").writeText("damage one")
        java.io.File(folder, "attempts.previous.cw").writeText("damage two")
        val result = store.load(); assertTrue(result.blocked); assertNull(result.library)
        assertEquals("damage one", java.io.File(folder,"attempts.cw").readText())
    }
    @Test fun failedWriteLeavesPreviousPrimaryReadable() {
        val folder = temporary.newFolder(); val store = AtomicLibraryStore(folder)
        val first = Library(listOf(daily())); store.save(first)
        java.io.File(folder, "attempts.pending.cw").mkdir()
        assertThrows(Exception::class.java) { store.save(Library()) }
        assertEquals(first, store.load().library)
    }
    @Test fun negativeEpochAndFarDatesAlwaysGiveValidIndex() {
        listOf(LocalDate.of(1900,1,1), LocalDate.of(2026,10,25), LocalDate.of(2100,1,1)).forEach {
            assertTrue(AttemptRules.dailyIndex("negative-hash", it, 10) in 0..9)
        }
    }
}
