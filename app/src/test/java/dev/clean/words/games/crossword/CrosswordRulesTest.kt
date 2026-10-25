package dev.clean.words.games.crossword

import dev.clean.words.core.PlayStatus
import org.junit.Assert.*
import org.junit.Test

class CrosswordRulesTest {
    private val puzzle = CrosswordContent.puzzles.first()
    private val board = requireNotNull(CrosswordRules.board(puzzle))

    @Test fun tenDistinctConnectedCluedGridsAreValid() {
        assertEquals(10, CrosswordContent.puzzles.size)
        assertEquals(10, CrosswordContent.puzzles.map { it.id }.distinct().size)
        assertEquals(10, CrosswordContent.puzzles.map { it.data["grid"] }.distinct().size)
        CrosswordContent.puzzles.forEach { record ->
            assertEquals("${record.id}: ${CrosswordRules.validate(record)}", emptyList<String>(), CrosswordRules.validate(record))
            val parsed = requireNotNull(CrosswordRules.board(record))
            assertEquals(25, parsed.grid.length)
            assertEquals(6, parsed.entries().size)
            assertEquals(15, parsed.grid.count { it != '#' })
            assertEquals(3, parsed.entries().count { it.direction == CrosswordDirection.ACROSS })
            assertEquals(3, parsed.entries().count { it.direction == CrosswordDirection.DOWN })
            assertTrue(record.data["origin"].orEmpty().isNotBlank())
        }
    }

    @Test fun crossingLettersUseOneCellAndSurviveMapCopy() {
        val across = board.entries().first { it.answer == "ERA" }
        val down = board.entries().first { it.answer == "APPLE" }
        val crossing = across.cells.intersect(down.cells.toSet()).single()
        val edited = CrosswordRules.enter(board, emptyMap(), crossing, "A")
        val restored = edited.toMap()
        assertEquals('A', CrosswordRules.cells(board, restored)[crossing])
        assertEquals(crossing, down.cells.first())
        val correction = CrosswordRules.enter(board, restored, crossing, "X")
        assertEquals('X', CrosswordRules.cells(board, correction)[crossing])
        assertEquals(PlayStatus.IN_PROGRESS, CrosswordRules.result(board, correction).status)
    }

    @Test fun checkMarksMistakesWithoutDeletingAndCorrectionClearsMark() {
        val wrong = CrosswordRules.enter(board, emptyMap(), 0, "Z")
        val checked = CrosswordRules.check(board, wrong)
        assertEquals('Z', CrosswordRules.cells(board, checked)[0])
        assertEquals(setOf(0), CrosswordRules.checked(checked))
        assertTrue(CrosswordRules.result(board, checked).assisted)
        val fixed = CrosswordRules.enter(board, checked, 0, "E")
        assertTrue(CrosswordRules.checked(fixed).isEmpty())
        assertEquals('E', CrosswordRules.cells(board, fixed)[0])
        assertTrue(CrosswordRules.result(board, fixed).assisted)
    }

    @Test fun fullWrongGridStaysUnfinishedAndSolvedGridLocks() {
        val wrong = mapOf("cells" to board.grid.map { if (it == '#') '#' else 'Z' }.joinToString(""))
        assertEquals(PlayStatus.IN_PROGRESS, CrosswordRules.result(board, wrong).status)
        val solved = mapOf("cells" to board.grid)
        assertEquals(PlayStatus.COMPLETED, CrosswordRules.result(board, solved).status)
        assertEquals(solved, CrosswordRules.enter(board, solved, 0, "Z"))
        assertEquals(solved, CrosswordRules.check(board, solved))
        assertEquals(PlayStatus.COMPLETED, CrosswordRules.result(board, solved.toMap()).status)
    }

    @Test fun illegalEditsDoNotChangeState() {
        val initial = mapOf("untouched" to "value")
        assertEquals(initial, CrosswordRules.enter(board, initial, 3, "A"))
        assertEquals(initial, CrosswordRules.enter(board, initial, -1, "A"))
        assertEquals(initial, CrosswordRules.enter(board, initial, 0, "7"))
        assertEquals(initial, CrosswordRules.enter(board, initial, 0, "AB"))
        val entered = CrosswordRules.enter(board, initial, 0, "e")
        assertEquals('E', CrosswordRules.cells(board, entered)[0])
        assertEquals('.', CrosswordRules.cells(board, CrosswordRules.enter(board, entered, 0, ""))[0])
    }

    @Test fun corruptStateCannotPopulateBlocksOrCrash() {
        val cells = CrosswordRules.cells(board, mapOf("cells" to "!", "checked" to "-1,99,nope"))
        assertEquals(25, cells.length)
        assertEquals('#', cells[3])
        assertEquals('.', cells[0])
        assertTrue(CrosswordRules.checked(mapOf("checked" to "-1,99,nope")).isEmpty())
    }

    @Test fun missingClueAndMalformedGridAreRejected() {
        assertTrue(CrosswordRules.validate(puzzle.copy(data = puzzle.data + ("grid" to "SHORT"))).isNotEmpty())
        assertTrue(CrosswordRules.validate(puzzle.copy(data = puzzle.data + ("clues" to ""))).isNotEmpty())
        assertTrue(CrosswordRules.validate(puzzle.copy(data = puzzle.data + ("grid" to "A########################"))).contains("Every open cell must belong to a word"))
    }

    @Test fun everyPuzzleCanBeCompletedByEnteringItsActualCells() {
        CrosswordContent.puzzles.forEach { record ->
            val b = requireNotNull(CrosswordRules.board(record))
            var state = emptyMap<String, String>()
            b.grid.indices.filter { b.grid[it] != '#' }.forEach { index ->
                state = CrosswordRules.enter(b, state, index, b.grid[index].toString())
            }
            assertEquals(record.id, PlayStatus.COMPLETED, CrosswordRules.result(b, state).status)
            assertTrue(CrosswordRules.checked(CrosswordRules.check(b, state)).isEmpty())
        }
    }
}
