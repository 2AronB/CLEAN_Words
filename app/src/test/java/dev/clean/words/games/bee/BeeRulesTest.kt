package dev.clean.words.games.bee

import dev.clean.words.core.PlayStatus
import org.junit.Assert.*
import org.junit.Test

class BeeRulesTest {
    private val puzzle = BeePuzzle.from(BeeContent.puzzles.first())

    @Test fun tenDistinctPuzzlesHaveValidatedGoalsPangramsAndPinnedDictionaries() {
        assertEquals(10, BeeContent.puzzles.size)
        assertEquals(10, BeeContent.puzzles.map { it.id }.distinct().size)
        assertEquals(10, BeeContent.puzzles.map { it.data["letters"] }.distinct().size)
        BeeContent.puzzles.forEach { record ->
            val board = BeePuzzle.from(record)
            assertEquals(record.id, emptyList<String>(), BeeRules.validate(board))
            assertEquals(record, board.snapshot(record.id))
            assertTrue(board.dictionaryVersion.startsWith("ENABLE1-sha256:"))
            assertTrue(board.validWords.size >= board.goalWords.size)
        }
    }

    @Test fun knownWordScoresAndDuplicateCannotScoreAgain() {
        val found = BeeRules.submit(puzzle, BeeState(), "later")
        assertTrue("LATER" in found.found)
        assertEquals(5, BeeRules.goalScore(puzzle, found))
        val duplicate = BeeRules.submit(puzzle, found, " LATER ")
        assertEquals(found.found, duplicate.found)
        assertEquals(5, BeeRules.goalScore(puzzle, duplicate))
    }

    @Test fun missingCenterWrongLettersShortAndNonLettersRejectedWithoutPoints() {
        val fixture = BeePuzzle("ACEHINT", 'A', setOf("CHAIN"), setOf("CHAIN", "HINT", "TRAIN"), "fixture")
        assertTrue("CHAIN" in BeeRules.submit(fixture, BeeState(), "CHAIN").found)
        listOf("HINT", "TRAIN", "CAT", "CHAIN!").forEach { word ->
            val rejected = BeeRules.submit(fixture, BeeState(entry = word))
            assertTrue(word, rejected.found.isEmpty())
            assertEquals(word, rejected.entry)
            assertTrue(rejected.message.isNotBlank())
        }
    }

    @Test fun fourLetterAndPangramScoresCorrect() {
        assertEquals(1, BeeRules.points(puzzle, "RATE"))
        assertEquals(5, BeeRules.points(puzzle, "LATER"))
        assertEquals(14, BeeRules.points(puzzle, "CENTRAL"))
        assertTrue(BeeRules.pangram(puzzle, "CENTRAL"))
        assertFalse(BeeRules.pangram(puzzle, "LATER"))
    }

    @Test fun bonusWordsAreAcceptedButDoNotDistortGoalDenominator() {
        val bonusWord = puzzle.validWords.first { it !in puzzle.goalWords }
        val found = BeeRules.submit(puzzle, BeeState(), bonusWord)
        assertTrue(bonusWord in found.found)
        assertEquals(0, BeeRules.goalScore(puzzle, found))
        assertEquals(BeeRules.points(puzzle, bonusWord), BeeRules.bonusScore(puzzle, found))
        assertEquals(puzzle.maximumGoalScore, BeeRules.result(puzzle, found).maxScore)
        assertEquals(PlayStatus.IN_PROGRESS, BeeRules.result(puzzle, found).status)
    }

    @Test fun allGoalsCompleteEveryPuzzleAndContinuedBonusPlayPreservesCompletion() {
        BeeContent.puzzles.forEach { record ->
            val board = BeePuzzle.from(record)
            var state = BeeState()
            board.goalWords.forEach { word -> state = BeeRules.submit(board, state, word) }
            assertEquals(board.maximumGoalScore, BeeRules.goalScore(board, state))
            assertEquals(PlayStatus.COMPLETED, BeeRules.result(board, state).status)
            val extra = board.validWords.firstOrNull { it !in board.goalWords }
            if (extra != null) state = BeeRules.submit(board, state, extra)
            assertEquals(PlayStatus.COMPLETED, BeeRules.result(board, state).status)
            assertEquals(state, BeeState.from(state.toMap()))
        }
    }

    @Test fun targetRoundsUpFromActualFamiliarGoalPointsOnly() {
        BeeContent.puzzles.forEach { record ->
            val board = BeePuzzle.from(record)
            val actualTotal = board.goalWords.sumOf { BeeRules.points(board, it) }
            assertEquals(actualTotal, board.maximumGoalScore)
            assertEquals((actualTotal * 7 + 9) / 10, board.target)
            assertTrue(board.target in 1..actualTotal)
        }
    }

    @Test fun partialEntryAndFoundWordsSurviveMapRoundTrip() {
        val state = BeeState(setOf("LATER", "RATE"), "CENT", "Try again")
        val restored = BeeState.from(state.toMap())
        assertEquals(state, restored)
        assertEquals(BeeRules.result(puzzle, state), BeeRules.result(puzzle, restored))
    }

    @Test fun dictionaryUnknownWordRejectedAndContentErrorsReported() {
        assertTrue(BeeRules.submit(puzzle, BeeState(), "AAAACCCC").found.isEmpty())
        assertTrue(BeeRules.validate(puzzle.copy(letters = "AAAAAAA")).isNotEmpty())
        assertTrue(BeeRules.validate(puzzle.copy(center = 'Z')).isNotEmpty())
        assertTrue(BeeRules.validate(puzzle.copy(goalWords = setOf("BADWORD"))).isNotEmpty())
        assertTrue(BeeRules.validate(puzzle.copy(dictionaryVersion = "")).isNotEmpty())
    }
}
