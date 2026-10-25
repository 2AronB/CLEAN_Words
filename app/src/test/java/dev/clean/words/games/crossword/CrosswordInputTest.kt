package dev.clean.words.games.crossword

import dev.clean.words.core.PlayStatus
import org.junit.Assert.*
import org.junit.Test

class CrosswordInputTest {
    private val board = requireNotNull(CrosswordRules.board(CrosswordContent.puzzles.first()))

    @Test fun aSingleImeBurstKeepsEveryCharacterAcrossClueBoundary() {
        val entered = CrosswordInput.type(board, emptyMap(), "ZZZZZ")
        val cells = CrosswordRules.cells(board, entered)
        assertEquals("ZZZ", cells.substring(0, 3))
        assertEquals("ZZ", cells.substring(10, 12))
        assertEquals(12, CrosswordInput.selected(board, entered))
        assertEquals(PlayStatus.IN_PROGRESS, CrosswordRules.result(board, entered).status)
        assertTrue(CrosswordRules.checked(entered).isEmpty())
        assertFalse(CrosswordRules.result(board, entered).assisted)
    }

    @Test fun separateRapidEventsAccumulateWithoutParentRoundTrips() {
        var state = emptyMap<String,String>()
        listOf("Z", "Y", "X", "W", "V").forEach { state = CrosswordInput.type(board, state, it) }
        val cells = CrosswordRules.cells(board, state)
        assertEquals("ZYX", cells.substring(0, 3))
        assertEquals("WV", cells.substring(10, 12))
    }

    @Test fun aFilledWrongWordAdvancesWithoutChecking() {
        val entered = CrosswordInput.type(board, emptyMap(), "ZZZ")
        assertEquals("MAPLE", CrosswordInput.current(board, entered)!!.answer)
        assertEquals(10, CrosswordInput.selected(board, entered))
        assertTrue(CrosswordRules.checked(entered).isEmpty())
        assertNull(entered["checkedOnce"])
        assertNull(entered["assisted"])
    }

    @Test fun existingCrossingsAreSkippedAndFilledClueAdvances() {
        var state = CrosswordRules.enter(board, emptyMap(), 0, "E")
        state = CrosswordRules.enter(board, state, 2, "A")
        state = CrosswordInput.select(board, state, 1, CrosswordDirection.ACROSS)
        state = CrosswordInput.type(board, state, "R")
        assertEquals("ERA", CrosswordRules.cells(board, state).substring(0,3))
        assertEquals("MAPLE", CrosswordInput.current(board, state)!!.answer)
        assertEquals(10, CrosswordInput.selected(board, state))
    }

    @Test fun downClueRetainsDirectionAndAdvancesAfterItsLastBlank() {
        val selected = CrosswordInput.select(board, emptyMap(), 2, CrosswordDirection.DOWN)
        val filled = CrosswordInput.type(board, selected, "APPLE")
        assertEquals("APPLE", listOf(2,7,12,17,22).map { CrosswordRules.cells(board, filled)[it] }.joinToString(""))
        assertEquals("EWE", CrosswordInput.current(board, filled)!!.answer)
    }

    @Test fun backspaceOnBlankClearsPreviousLetterAndMovesThere() {
        val entered = CrosswordInput.type(board, emptyMap(), "ER")
        assertEquals(2, CrosswordInput.selected(board, entered))
        val erased = CrosswordInput.delete(board, entered)
        assertEquals("E..", CrosswordRules.cells(board, erased).substring(0,3))
        assertEquals(1, CrosswordInput.selected(board, erased))
        val again = CrosswordInput.delete(board, erased)
        assertEquals("...", CrosswordRules.cells(board, again).substring(0,3))
        assertEquals(0, CrosswordInput.selected(board, again))
    }

    @Test fun backspaceOnFilledClearsCurrentAndMovesPrevious() {
        var entered = CrosswordRules.enter(board, emptyMap(), 0, "E")
        entered = CrosswordRules.enter(board, entered, 1, "R")
        entered = CrosswordInput.select(board, entered, 1, CrosswordDirection.ACROSS)
        val erased = CrosswordInput.delete(board, entered)
        assertEquals("E..", CrosswordRules.cells(board, erased).substring(0,3))
        assertEquals(0, CrosswordInput.selected(board, erased))
    }

    @Test fun deleteAtStartStepsToPreviousClue() {
        val typed = CrosswordInput.type(board, emptyMap(), "ERA")
        assertEquals(10, CrosswordInput.selected(board, typed))
        val erased = CrosswordInput.delete(board, typed)
        assertEquals(2, CrosswordInput.selected(board, erased))
        assertEquals("ER.", CrosswordRules.cells(board, erased).substring(0,3))
    }

    @Test fun clearOnlyCurrentClueAndEnterNeverCheck() {
        var state = CrosswordInput.type(board, emptyMap(), "ERAZZ")
        state = CrosswordInput.clearClue(board, state)
        assertEquals("ERA", CrosswordRules.cells(board, state).substring(0,3))
        assertEquals(".....", CrosswordRules.cells(board, state).substring(10,15))
        val moved = CrosswordInput.nextClue(board, state)
        assertNotEquals(CrosswordInput.current(board, state), CrosswordInput.current(board, moved))
        assertTrue(CrosswordRules.checked(moved).isEmpty())
        assertFalse(CrosswordRules.result(board, moved).assisted)
    }

    @Test fun inputBufferHandlesPasteBackspaceAndSentinelDeletion() {
        assertEquals(CrosswordInput.BufferEdit(0, "CDEF"), CrosswordInput.bufferEdit("\u200BAB", "\u200BABCDEF"))
        assertEquals(CrosswordInput.BufferEdit(1, ""), CrosswordInput.bufferEdit("\u200BAB", "\u200BA"))
        assertEquals(CrosswordInput.BufferEdit(1, ""), CrosswordInput.bufferEdit("\u200B", ""))
        assertEquals(CrosswordInput.BufferEdit(0, "AB"), CrosswordInput.bufferEdit("\u200B", "ab"))
        assertEquals(CrosswordInput.BufferEdit(0, ""), CrosswordInput.bufferEdit("\u200B", "\u200B"))
    }

    @Test fun cursorAndTypedMovesRestoreWithoutResettingCrossings() {
        val state = CrosswordInput.type(board, emptyMap(), "ERAZZ").toMap()
        val restored = state.toMap()
        assertEquals(CrosswordRules.cells(board,state), CrosswordRules.cells(board,restored))
        assertEquals(CrosswordInput.current(board,state), CrosswordInput.current(board,restored))
        assertEquals(CrosswordInput.selected(board,state), CrosswordInput.selected(board,restored))
    }

    @Test fun fullIncorrectGridRemainsEditableWithoutAutomaticMarks() {
        val state = mapOf("cells" to board.grid.map { if (it == '#') '#' else 'Z' }.joinToString(""))
        val moved = CrosswordInput.nextClue(board,state)
        val edited = CrosswordInput.type(board,moved,"X")
        assertEquals(PlayStatus.IN_PROGRESS, CrosswordRules.result(board,edited).status)
        assertTrue(CrosswordRules.checked(edited).isEmpty())
        assertFalse(CrosswordRules.result(board,edited).assisted)
    }
}
