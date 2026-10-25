package dev.clean.words.games.strands

import dev.clean.words.core.PlayStatus
import org.junit.Assert.*
import org.junit.Test

class StrandsRulesTest {
    private val puzzle = StrandsPuzzle.from(StrandsContent.puzzles.first())

    @Test fun tenDistinctOriginalBoardsHaveValidCompletePartitions() {
        assertEquals(10, StrandsContent.puzzles.size)
        assertEquals(10, StrandsContent.puzzles.map { it.id }.distinct().size)
        assertEquals(10, StrandsContent.puzzles.map { it.data["letters"] }.distinct().size)
        StrandsContent.puzzles.forEach { record ->
            assertEquals(record.id, emptyList<String>(), StrandsRules.validate(StrandsPuzzle.from(record)))
            assertEquals(record, StrandsPuzzle.from(record).snapshot(record.id))
        }
    }

    @Test fun authoredAnswersCompleteEveryBoardOnceAndRestore() {
        StrandsContent.puzzles.forEach { record ->
            val board = StrandsPuzzle.from(record)
            var progress = StrandsState()
            board.answers.forEach { answer ->
                progress = StrandsRules.submit(board, progress.copy(selected = answer.path))
                progress = StrandsState.from(progress.toMap())
            }
            assertEquals(record.id, PlayStatus.COMPLETED, StrandsRules.result(board, progress).status)
            assertEquals(board.answers.size, StrandsRules.result(board, progress).score)
            assertEquals(progress, StrandsRules.submit(board, progress))
            assertEquals(progress, StrandsRules.tap(board, progress, 0))
        }
    }

    @Test fun diagonalAllowedButJumpReuseAndRowWrapRejected() {
        assertTrue(StrandsRules.legalPath(puzzle, listOf(0, puzzle.columns + 1)))
        assertFalse(StrandsRules.legalPath(puzzle, listOf(0, 2)))
        assertFalse(StrandsRules.legalPath(puzzle, listOf(0, 1, 0)))
        assertFalse(StrandsRules.legalPath(puzzle, listOf(puzzle.columns - 1, puzzle.columns)))
        assertFalse(StrandsRules.legalPath(puzzle, listOf(-1)))
        assertFalse(StrandsRules.legalPath(puzzle, emptyList()))
    }

    @Test fun badSelectionNeverLosesFoundWordsAndTappingBackRestoresPath() {
        val answer = puzzle.answers.first()
        val found = StrandsRules.submit(puzzle, StrandsState(selected = answer.path))
        val invalid = StrandsRules.submit(puzzle, found.copy(selected = listOf(0, 2)))
        assertEquals(found.found, invalid.found)
        assertEquals(PlayStatus.IN_PROGRESS, StrandsRules.result(puzzle, invalid).status)
        val tap = StrandsRules.tap(puzzle, StrandsState(), answer.path[0])
        val tapTwo = StrandsRules.tap(puzzle, tap, answer.path[1])
        assertEquals(listOf(answer.path[0]), StrandsRules.tap(puzzle, tapTwo, answer.path[1]).selected)
        assertEquals(emptyList<Int>(), StrandsRules.tap(puzzle, tap, answer.path[0]).selected)
    }

    @Test fun duplicateWordAndOverlapCannotCountTwice() {
        val answer = puzzle.answers.first()
        val found = StrandsRules.submit(puzzle, StrandsState(selected = answer.path))
        assertEquals(1, StrandsRules.submit(puzzle, found.copy(selected = answer.path)).found.size)
        val invented = found.copy(found = found.found + found.found)
        assertFalse(StrandsRules.validFound(puzzle, invented))
        assertEquals(0, StrandsRules.result(puzzle, invented).score)
    }

    @Test fun undoReturnsFoundCellsWithoutLosingEarlierWords() {
        val one = StrandsRules.submit(puzzle, StrandsState(selected = puzzle.answers[0].path))
        val two = StrandsRules.submit(puzzle, one.copy(selected = puzzle.answers[1].path))
        val undone = StrandsRules.undo(two)
        assertEquals(one.found, undone.found)
        assertTrue(undone.selected.isEmpty())
        assertEquals(undone, StrandsState.from(undone.toMap()))
    }

    @Test fun stateSnapshotIncludesPartialSelectionAndMessage() {
        val state = StrandsState(listOf(puzzle.answers.first()), puzzle.answers[1].path.take(2), "Try another route")
        assertEquals(state, StrandsState.from(state.toMap()))
    }

    @Test fun contentValidationRejectsBadGeometrySpellingAndSpangram() {
        assertTrue(StrandsRules.validate(puzzle.copy(letters = puzzle.letters.dropLast(1))).isNotEmpty())
        assertTrue(StrandsRules.validate(puzzle.copy(spangram = "MISSING")).isNotEmpty())
        assertTrue(StrandsRules.validate(puzzle.copy(answers = puzzle.answers + puzzle.answers.first())).isNotEmpty())
        assertFalse(StrandsRules.spans(puzzle, listOf(0, 1)))
        assertTrue(StrandsRules.spans(puzzle, puzzle.answers.first().path))
    }

    @Test fun alternateSpellingThatBlocksOtherAnswersIsNotCommitted() {
        val garden = StrandsPuzzle.from(StrandsContent.puzzles.first { it.id == "strands-garden-v1" })
        val route = listOf(19, 20, 14, 8, 2, 3, 9)
        assertEquals("FLOWERS", StrandsRules.spelling(garden, route))
        assertTrue(StrandsRules.legalPath(garden, route))
        assertEquals(StrandCompletionCheck.BLOCKED,
            StrandsRules.completionCheck(garden, listOf(StrandAnswer("FLOWERS", route))))
        val state = StrandsState(selected = route)
        val rejected = StrandsRules.submit(garden, state)
        assertEquals(state.found, rejected.found)
        assertEquals(route, rejected.selected)
        assertTrue(rejected.message.contains("blocks the remaining words"))
    }

    @Test fun alternateRouteIsAcceptedWhenTheRestStillFits() {
        val sports = StrandsPuzzle.from(StrandsContent.puzzles.first { it.id == "strands-sports-v1" })
        val route = listOf(19, 20, 15, 14, 21, 22)
        assertEquals("TENNIS", StrandsRules.spelling(sports, route))
        assertNotEquals(sports.answers.first { it.word == "TENNIS" }.path, route)
        val accepted = StrandsRules.submit(sports, StrandsState(selected = route))
        assertEquals(listOf(StrandAnswer("TENNIS", route)), accepted.found)
        assertTrue(accepted.selected.isEmpty())
        assertEquals(StrandCompletionCheck.SOLVABLE, StrandsRules.completionCheck(sports, accepted.found))
    }

    @Test fun alternateRouteMayRerouteOtherAnswersAndStillCompleteWholeBoard() {
        val colors = StrandsPuzzle.from(StrandsContent.puzzles.first { it.id == "strands-colors-v1" })
        val purple = StrandAnswer("PURPLE", listOf(19, 20, 14, 15, 10, 17))
        val canonical = colors.answers.first { it.word == purple.word }
        assertNotEquals(canonical.path.toSet(), purple.path.toSet())
        assertEquals(StrandCompletionCheck.SOLVABLE, StrandsRules.completionCheck(colors, listOf(purple)))
        var state = StrandsRules.submit(colors, StrandsState(selected = purple.path))
        assertEquals(listOf(purple), state.found)
        val rest = listOf(
            colors.answers.first { it.word == "COLORS" },
            StrandAnswer("YELLOW", listOf(23, 22, 21, 16, 11, 5)),
            colors.answers.first { it.word == "ORANGE" },
        )
        rest.forEach { answer -> state = StrandsRules.submit(colors, state.copy(selected = answer.path)) }
        assertEquals(PlayStatus.COMPLETED, StrandsRules.result(colors, state).status)
        assertEquals(state, StrandsState.from(state.toMap()))
    }

    @Test fun deleteRemovesOnePathStepAndClearNeverRemovesFoundWords() {
        val initial = StrandsState(found = listOf(puzzle.answers.first()), selected = puzzle.answers[1].path.take(3))
        val deleted = StrandsRules.delete(initial)
        assertEquals(initial.selected.take(2), deleted.selected)
        assertEquals(initial.found, deleted.found)
        val cleared = StrandsRules.clear(initial)
        assertTrue(cleared.selected.isEmpty())
        assertEquals(initial.found, cleared.found)
    }

    @Test fun boundedSearchReportsUnknownWithoutCommittingUncheckedRoute() {
        val garden = StrandsPuzzle.from(StrandsContent.puzzles.first { it.id == "strands-garden-v1" })
        val route = listOf(19, 20, 14, 8, 2, 3, 9)
        assertEquals(StrandCompletionCheck.LIMIT_REACHED,
            StrandsRules.completionCheck(garden, listOf(StrandAnswer("FLOWERS", route)), maximumSteps = 0))
    }
}
