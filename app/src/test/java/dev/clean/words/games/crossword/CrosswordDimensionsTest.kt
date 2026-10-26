package dev.clean.words.games.crossword

import dev.clean.words.core.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

/** Independent authored fixture: neither generator slots nor numbering are used to build it. */
class CrosswordDimensionsTest {
    private val grid = listOf(
        "BAR####", "U#I####", "SAVER##", "##E#A##", "##RACES", "####E#O", "####ROD",
    ).joinToString("")
    private val puzzle = PuzzleRecord("dimensions-fixture", title = "Seven-square test", data = mapOf(
        "width" to "7", "height" to "7", "grid" to grid,
        "clues" to listOf(
            "BAR|Place to order a drink", "BUS|Public road transport", "RIVER|Flowing waterway",
            "SAVER|One who puts money aside", "RACER|Competitor on a track",
            "RACES|Contests of speed", "SOD|Grass with its soil", "ROD|Fishing pole",
        ).joinToString("\n"),
    ))
    private val board get() = requireNotNull(CrosswordRules.board(puzzle))

    @Test fun sevenSquareEntriesHaveCorrectRowBoundariesDownStrideAndSharedNumbering() {
        assertEquals(emptyList<String>(), CrosswordRules.validate(puzzle))
        assertEquals(7, board.width)
        assertEquals(7, board.height)
        val actual = board.entries().associate { "${it.number}${it.direction.name.first()}" to (it.answer to it.cells) }
        assertEquals(mapOf(
            "1A" to ("BAR" to listOf(0, 1, 2)),
            "1D" to ("BUS" to listOf(0, 7, 14)),
            "2D" to ("RIVER" to listOf(2, 9, 16, 23, 30)),
            "3A" to ("SAVER" to listOf(14, 15, 16, 17, 18)),
            "4D" to ("RACER" to listOf(18, 25, 32, 39, 46)),
            "5A" to ("RACES" to listOf(30, 31, 32, 33, 34)),
            "6D" to ("SOD" to listOf(34, 41, 48)),
            "7A" to ("ROD" to listOf(46, 47, 48)),
        ), actual)
    }

    @Test fun checkAndCorrectionRetainMistakesBeyondLegacyTwentyFiveCells() {
        var state = CrosswordRules.enter(board, emptyMap(), 48, "Z")
        state = CrosswordRules.check(board, state)
        assertEquals(setOf(48), CrosswordRules.checked(state))
        assertEquals('Z', CrosswordRules.cells(board, state)[48])
        assertTrue(CrosswordRules.result(board, state).assisted)
        state = CrosswordRules.enter(board, state, 0, "X")
        assertEquals("Unrelated edits must retain the high-index mistake", setOf(48), CrosswordRules.checked(state))
        state = CrosswordRules.enter(board, state, 48, "D")
        assertTrue(CrosswordRules.checked(state).isEmpty())
        assertTrue(CrosswordRules.result(board, state).assisted)
    }

    @Test fun pasteAndDeleteCrossClueBoundariesAtHighIndicesWithoutChecking() {
        val selected = CrosswordInput.select(board, emptyMap(), 46, CrosswordDirection.ACROSS)
        val typed = CrosswordInput.type(board, selected, "XYZAB")
        val cells = CrosswordRules.cells(board, typed)
        assertEquals("XYZ", cells.substring(46, 49))
        assertEquals('A', cells[0])
        assertEquals('B', cells[7])
        assertEquals(14, CrosswordInput.selected(board, typed))
        assertEquals(CrosswordDirection.DOWN, CrosswordInput.direction(typed))
        val erased = CrosswordInput.delete(board, typed)
        assertEquals('.', CrosswordRules.cells(board, erased)[7])
        assertEquals(7, CrosswordInput.selected(board, erased))
        assertNull(erased["checkedOnce"])
        assertFalse(CrosswordRules.result(board, erased).assisted)
    }

    @Test fun malformedOrMissingDimensionsNeverReinterpretSavedGrid() {
        listOf(
            puzzle.data - "width" - "height", puzzle.data - "width", puzzle.data - "height",
            puzzle.data + ("width" to "5"), puzzle.data + ("height" to "0"),
            puzzle.data + ("width" to "bogus"), puzzle.data + ("width" to Int.MAX_VALUE.toString()),
            puzzle.data + ("grid" to grid.dropLast(1)),
        ).forEach { data -> assertNull("Must reject dimensions ${data["width"]}x${data["height"]}", CrosswordRules.board(puzzle.copy(data = data))) }
        val legacy = requireNotNull(CrosswordRules.board(CrosswordContent.puzzles.first()))
        assertEquals(5, legacy.width)
        assertEquals(5, legacy.height)
        assertEquals(25, legacy.grid.length)
    }

    @Test fun legacyAndLargerSnapshotsAndResultsRoundTripTogetherWithoutRejudging() {
        val now = Instant.parse("2026-10-26T12:00:00Z")
        val zone = ZoneId.of("UTC")
        val legacy = AttemptRules.create(Library(), CrosswordGame, PuzzleMode.DAILY, now, zone)
        var larger = AttemptRules.createSnapshot(Library(listOf(legacy)), CrosswordGame, PuzzleMode.EXTRA, puzzle, now, zone)
        val edited = CrosswordInput.type(board, CrosswordInput.select(board, emptyMap(), 46, CrosswordDirection.ACROSS), "RO")
        larger = AttemptRules.update(larger, edited, CrosswordGame, now.plusSeconds(1))
        val original = Library(listOf(legacy, larger))
        val restored = LibraryCodec.decode(LibraryCodec.encode(original))
        assertEquals(original, restored)
        assertEquals(legacy, AttemptRules.createSnapshot(restored, CrosswordGame, PuzzleMode.DAILY, puzzle, now, zone))
        val reopened = restored.attempts.last()
        assertEquals(48, CrosswordInput.selected(board, reopened.state))
        assertEquals("RO.", CrosswordRules.cells(board, reopened.state).substring(46, 49))
        val solved = AttemptRules.update(reopened, mapOf("cells" to grid), CrosswordGame, now.plusSeconds(2))
        assertEquals(PlayStatus.COMPLETED, solved.result.status)
        assertEquals(22, solved.result.score)
        assertEquals(now.plusSeconds(2).toString(), solved.completedAt)
        assertEquals(solved, LibraryCodec.decode(LibraryCodec.encode(Library(listOf(solved)))).attempts.single())
    }
}
