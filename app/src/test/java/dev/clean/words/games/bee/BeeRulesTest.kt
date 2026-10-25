package dev.clean.words.games.bee

import dev.clean.words.core.PlayStatus
import org.junit.Assert.*
import org.junit.Test

class BeeRulesTest {
    private val puzzle = BeePuzzle.from(BeeContent.puzzles.first()).copy(scoringVersion = BeePuzzle.LEGACY_SCORING)

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

    @Test fun newRecordsUseNewRevisionAndCountEveryAcceptedWordEqually() {
        BeeContent.puzzles.forEach { record ->
            val board = BeePuzzle.from(record)
            assertEquals(2, record.revision)
            assertEquals(BeePuzzle.WORD_COUNT_SCORING, board.scoringVersion)
            val formerlyBonus = board.validWords.first { it !in board.goalWords }
            val seed = board.goalWords.first()
            val first = BeeRules.submit(board, BeeState(), formerlyBonus)
            val second = BeeRules.submit(board, first, seed)
            assertEquals(1, BeeRules.result(board, first).score)
            assertEquals(2, BeeRules.result(board, second).score)
            assertEquals(board.wordTarget, BeeRules.result(board, first).maxScore)
            assertEquals((board.goalWords.size * 7 + 9) / 10, board.wordTarget)
            assertFalse(second.message.contains("bonus"))
        }
    }

    @Test fun formerlyBonusWordsAloneCanCompleteDailyGoalWithoutHiddenMembership() {
        val board = puzzle.copy(scoringVersion = BeePuzzle.WORD_COUNT_SCORING)
        var state = BeeState()
        board.validWords.filter { it !in board.goalWords }.take(board.wordTarget).forEach {
            state = BeeRules.submit(board, state, it)
        }
        assertEquals(0, BeeRules.goalScore(board, state))
        assertEquals(PlayStatus.COMPLETED, BeeRules.result(board, state).status)
        assertEquals(board.wordTarget, BeeRules.result(board, state).score)
    }

    @Test fun lengthBarsUseCompleteAcceptedDenominatorsAndIgnoreDuplicatesOrInvalidStateWords() {
        val board = BeePuzzle("ACEHINT", 'A', setOf("CHAIN"), setOf("CHAIN", "CHANT", "ACHE", "ACNE"), "fixture", BeePuzzle.WORD_COUNT_SCORING)
        val state = BeeState(setOf("CHAIN", "ACHE", "ZZZZ"))
        assertEquals(listOf(BeeLengthProgress(4,1,2), BeeLengthProgress(5,1,2)), BeeRules.lengthProgress(board, state))
        val repeated = BeeRules.submit(board, state, "CHAIN")
        assertEquals(BeeRules.lengthProgress(board, state), BeeRules.lengthProgress(board, repeated))
        assertEquals(2, BeeRules.result(board, state).score)
        assertEquals(6, BeeRules.totalScore(board, state))
    }

    @Test fun legacySnapshotsKeepOriginalScoringAndResultSummary() {
        val saved = puzzle.snapshot("bee-central-v1")
        assertEquals(1, saved.revision)
        val restored = BeePuzzle.from(saved)
        val state = BeeRules.submit(restored, BeeState(), "LATER")
        assertTrue(restored.legacy)
        assertEquals(dev.clean.words.core.GameResult(PlayStatus.IN_PROGRESS,
            "Goal: 5/${restored.target} points · 0 bonus points", 5, restored.maximumGoalScore), BeeRules.result(restored, state))
        assertEquals(5, BeeRules.result(restored, state).score)
        assertEquals(puzzle.maximumGoalScore, BeeRules.result(restored, state).maxScore)
        assertEquals(saved, restored.snapshot(saved.id))
    }

    @Test fun countVersionRejectsUnknownRulesAndPreservesSnapshotVersion() {
        assertTrue(BeeRules.validate(puzzle.copy(scoringVersion = "future" )).contains("Unsupported scoring version"))
        val new = puzzle.copy(scoringVersion = BeePuzzle.WORD_COUNT_SCORING).snapshot("new")
        assertEquals(new, BeePuzzle.from(new).snapshot(new.id))
    }

    @Test fun polygonGeometryHasCenterAndDistinctOuterCellsInsideTheBoard() {
        (3..8).forEach { sides ->
            val positions = BeeGeometry.positions(sides)
            val radius = BeeGeometry.diameter(sides) / 2
            assertEquals(sides + 1, positions.size)
            assertEquals(positions.size, positions.distinct().size)
            assertEquals(BeeCellPosition(.5f, .5f), positions.first())
            positions.forEach { assertTrue(it.x - radius >= 0 && it.x + radius <= 1 && it.y - radius >= 0 && it.y + radius <= 1) }
        }
        assertEquals(4, BeeGeometry.positions(3).size)
        assertEquals(5, BeeGeometry.positions(4).size)
        assertEquals(7, BeeGeometry.positions(6).size)
    }

    @Test fun rapidLetterCallbacksAccumulateBeforeParentAcknowledgement() {
        val input = BeeInputBuffer(BeeState())
        val first = input.edit { it.copy(entry = it.entry + 'C') }
        val second = input.edit { it.copy(entry = it.entry + 'A') }
        val third = input.edit { it.copy(entry = it.entry + 'T') }
        assertEquals("CAT", input.current.entry)
        assertEquals("CAT", input.acceptExternal(BeeState()).entry)
        assertEquals("CAT", input.acceptExternal(first).entry)
        assertEquals("CAT", input.acceptExternal(second).entry)
        assertEquals("CAT", input.acceptExternal(third).entry)
    }

    @Test fun rapidSubmitThenTypingDoesNotLoseFoundWordsAndRecoveryCanReplaceState() {
        val input = BeeInputBuffer(BeeState(entry = "LATER"))
        val accepted = input.edit { BeeRules.submit(puzzle, it) }
        input.edit { it.copy(entry = "RATE") }
        assertTrue("LATER" in input.current.found)
        assertEquals("RATE", input.acceptExternal(accepted).entry)
        val recovery = BeeState(setOf("CENTRAL"), "C")
        assertEquals(recovery, input.acceptExternal(recovery))
    }
}
