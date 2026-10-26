package dev.clean.words.games.bee

import dev.clean.words.core.Attempt
import dev.clean.words.core.GameResult
import dev.clean.words.core.Library
import dev.clean.words.core.LibraryCodec
import dev.clean.words.core.PlayStatus
import dev.clean.words.core.PuzzleMode
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class BeeOpeningTest {
    private val original = BeePuzzle.from(BeeContent.puzzles.first())
    private val pins = mapOf("generator" to "fixture-generator-v3", "profile" to "fixture-profile-v3",
        "profileThresholds" to "fixture-thresholds", "sourceIndex" to "fixture-index",
        "sourceIndexSha256" to "fixture-hash", "familiarityVersion" to "fixture-ai-editorial-v1")
    private fun freeze(board: BeePuzzle) = BeeOpening.freeze(board, pins,
        BeeOpeningQuality(0, "test-only-not-production"))
    private fun board(familiarCount: Int = original.goalWords.size): BeePuzzle = freeze(original.copy(
        goalWords = (listOf("CENTRAL") + original.goalWords.sorted().filter { it != "CENTRAL" }).take(familiarCount).toSet()))
    private fun submit(board: BeePuzzle, words: List<String>, initial: BeeState = BeeState()): BeeState =
        words.fold(initial) { state, word -> BeeRules.submit(board, state, word) }
    private fun words(board: BeePuzzle, count: Int = 16): List<String> = board.validWords.sortedDescending().take(count)

    @Test fun orderedSubmissionsSurviveStateAndFullLibraryCodecWithoutAlphabeticalReordering() {
        val board = board()
        val order = listOf("RATE", "CENTRAL", "LATER", "CARE", "CLEAN", "ALTER", "RACE", "AREA", "LEARN", "CATER", "LACE")
        val state = submit(board, order).copy(entry = "CENT", message = "Draft retained")
        assertEquals(order, state.acceptedOrder)
        assertFalse(state.toMap().containsKey("found"))
        assertEquals(state, BeeState.from(state.toMap()))
        val time = "2026-10-26T12:00:00Z"
        val saved = Library(listOf(Attempt("opening-codec", "bee", PuzzleMode.EXTRA, null,
            board.snapshot("fixture-v3"), state.toMap(), time, time,
            if (BeeRules.result(board, state).status == PlayStatus.COMPLETED) time else null,
            BeeRules.result(board, state))))
        val restored = LibraryCodec.decode(LibraryCodec.encode(saved))
        assertEquals(saved, restored)
        val attempt = restored.attempts.single()
        val restoredBoard = BeePuzzle.from(attempt.puzzle)
        assertEquals(board.snapshot("fixture-v3"), restoredBoard.snapshot("fixture-v3"))
        assertEquals(order.take(10), BeeOpening.progress(restoredBoard, BeeState.from(attempt.state)).words)
        assertEquals(BeeOpening.progress(board, state), BeeOpening.progress(restoredBoard, BeeState.from(attempt.state)))
    }

    @Test fun rejectedAndDuplicateWordsNeverConsumeOpeningSlotsOrEraseDraft() {
        val board = board()
        var state = BeeRules.submit(board, BeeState(), " rate ")
        listOf("RATE", "rate", "RAT", "RATE!", "ZZZZ", "TREE", "AAAACCCC").forEach { attempted ->
            val before = state
            state = BeeRules.submit(board, state.copy(entry = attempted))
            assertEquals(listOf("RATE"), state.acceptedOrder)
            assertEquals(before.found, state.found)
            assertEquals(attempted, state.entry)
            assertEquals(1, BeeOpening.progress(board, state).words.size)
        }
        assertEquals(1, BeeOpening.progress(board, state).points)
    }

    @Test fun wordPointsPangramBonusAndBenchmarkTiesUseExactDeterministicRules() {
        val board = board()
        assertEquals(1, BeeRules.points(board, "RATE"))
        assertEquals(5, BeeRules.points(board, "LATER"))
        assertEquals(14, BeeRules.points(board, "CENTRAL"))
        assertEquals(10, BeeRules.points(board, "TARANTELLA"))
        assertFalse(BeeRules.pangram(board, "TARANTELLA"))
        val independentlySorted = board.validWords.sortedWith(Comparator { first, second ->
            fun points(word: String) = (if (word.length == 4) 1 else word.length) +
                if (word.toSet() == board.letters.toSet()) 7 else 0
            val scoreComparison = points(second).compareTo(points(first))
            if (scoreComparison == 0) first.compareTo(second) else scoreComparison
        }).take(10)
        assertEquals(independentlySorted, BeeOpening.bestTen(board))
        assertEquals(independentlySorted.sumOf { BeeRules.points(board, it) }, BeeOpening.benchmark(board))
        assertEquals(BeeOpening.bestTen(board), BeeOpening.bestTen(board.copy(validWords = board.validWords.reversed().toSet())))
        assertEquals(BeeOpening.bestTen(board).joinToString(","), board.openingData["benchmarkWords"])
    }

    @Test fun percentageUsesHalfUpTwoDecimalsAndTheBestTenIsExactlyOneHundred() {
        // 15-point repeated-letter pangram + two five-letter words + seven four-letter words = 32.
        val accepted = setOf("ACELNRTA", "ALERT", "ALTER", "AREA", "CARE", "LACE", "LATE", "NEAR", "RACE", "RATE")
        val board = freeze(BeePuzzle("ACELNRT", 'A', accepted, accepted, "fixture-dictionary", BeePuzzle.WORD_COUNT_SCORING))
        assertEquals(32, BeeOpening.benchmark(board))
        val first = BeeRules.submit(board, BeeState(), "RATE")
        assertEquals(BigDecimal("3.13"), BeeOpening.progress(board, first).percent)
        assertFalse(BeeOpening.progress(board, first).recorded)
        val perfect = submit(board, BeeOpening.bestTen(board))
        assertEquals(BigDecimal("100.00"), BeeOpening.progress(board, perfect).percent)
        assertTrue(BeeOpening.progress(board, perfect).recorded)
        assertEquals(BigDecimal("0.00"), BeeOpening.progress(board, BeeState()).percent)
    }

    @Test fun exactlyFirstTenFreezeOnceAndLaterBetterWordsCannotReplaceEarlierChoices() {
        val board = board()
        val low = board.validWords.sortedWith(compareBy<String> { BeeRules.points(board, it) }.thenBy { it }).take(10)
        var state = BeeState()
        low.forEachIndexed { index, word ->
            state = BeeRules.submit(board, state, word)
            val progress = BeeOpening.progress(board, state)
            assertEquals(index == 9, progress.recorded)
            assertEquals(low.take(index + 1), progress.words)
            assertTrue(progress.percent >= BigDecimal.ZERO && progress.percent <= BigDecimal(100))
        }
        val recorded = BeeOpening.progress(board, state)
        val later = BeeOpening.bestTen(board).filter { it !in low }
        state = submit(board, later, state)
        val explored = BeeOpening.progress(board, state)
        assertEquals(recorded.words, explored.words)
        assertEquals(recorded.points, explored.points)
        assertEquals(recorded.percent, explored.percent)
        assertTrue(explored.allWordPoints > recorded.allWordPoints)
        assertEquals(10 + later.size, explored.foundCount)
        assertEquals(explored, BeeOpening.progress(board, BeeState.from(state.toMap())))
    }

    @Test fun unfinishedOpeningCanResumeAfterRecreationAndFinalizeWithItsOriginalFirstNine() {
        val board = board(14)
        val ordered = words(board, 11)
        val firstNine = submit(board, ordered.take(9)).copy(entry = ordered[9].take(3))
        val saved = firstNine.toMap()
        val restored = BeeState.from(saved)
        assertEquals(firstNine, restored)
        assertFalse(BeeOpening.progress(board, restored).recorded)
        val tenth = BeeRules.submit(board, restored, ordered[9])
        assertEquals(ordered.take(10), BeeOpening.progress(board, tenth).words)
        assertTrue(BeeOpening.progress(board, tenth).recorded)
        assertEquals(PlayStatus.COMPLETED, BeeRules.result(board, tenth).status)
        val continued = BeeRules.submit(board, BeeState.from(tenth.toMap()), ordered[10])
        assertEquals(BeeOpening.progress(board, tenth).percent, BeeOpening.progress(board, continued).percent)
    }

    @Test fun completionBeforeAtAndAfterTenthWordStaysSeparateFromOpeningFinality() {
        listOf(10 to 7, 14 to 10, 16 to 12).forEach { (familiarCount, target) ->
            val board = board(familiarCount)
            assertEquals(target, board.wordTarget)
            var state = BeeState()
            words(board, 18).forEachIndexed { index, word ->
                state = BeeRules.submit(board, state, word)
                val count = index + 1
                val progress = BeeOpening.progress(board, state)
                assertEquals("familiar=$familiarCount count=$count", count >= target, progress.goalReached)
                assertEquals(count >= 10, progress.recorded)
                assertEquals(if (count >= target) PlayStatus.COMPLETED else PlayStatus.IN_PROGRESS, BeeRules.result(board, state).status)
                assertNull(BeeRules.result(board, state).score)
                assertNull(BeeRules.result(board, state).maxScore)
                if (count == target && count < 10) {
                    assertTrue(state.message.contains("opening still unfinished"))
                    assertTrue(BeeRules.result(board, state).summary.contains("Provisional opening"))
                }
            }
            assertEquals(PlayStatus.COMPLETED, BeeRules.result(board, state).status)
        }
    }

    @Test fun completionCreditIsEqualForWordsOutsideFamiliarSetAndRemainsStickyDuringExploration() {
        val board = board(10)
        val nonFamiliar = board.validWords.filter { it !in board.goalWords }.take(15)
        var state = submit(board, nonFamiliar.take(board.wordTarget))
        assertTrue(state.found.intersect(board.goalWords).isEmpty())
        assertEquals(PlayStatus.COMPLETED, BeeRules.result(board, state).status)
        assertFalse(BeeOpening.progress(board, state).recorded)
        state = submit(board, nonFamiliar.drop(board.wordTarget), state)
        assertEquals(PlayStatus.COMPLETED, BeeRules.result(board, state).status)
        assertTrue(BeeOpening.progress(board, state).recorded)
    }

    @Test fun rapidSubmissionsAndParentAcknowledgementsRetainExactOrderAndDraftActionsDoNotDeleteAcceptedWords() {
        val board = board()
        val input = BeeInputBuffer(BeeState())
        val first = input.edit { BeeRules.submit(board, it, "RATE") }
        val second = input.edit { BeeRules.submit(board, it, "CENTRAL") }
        val third = input.edit { BeeRules.submit(board, it, "LATER") }
        input.edit { it.copy(entry = "CARE") }
        assertEquals(listOf("RATE", "CENTRAL", "LATER"), input.acceptExternal(first).acceptedOrder)
        assertEquals("CARE", input.acceptExternal(second).entry)
        assertEquals("CARE", input.acceptExternal(third).entry)
        val cleared = input.edit { it.copy(entry = "", message = "") }
        assertEquals(listOf("RATE", "CENTRAL", "LATER"), cleared.acceptedOrder)
        input.edit { it.copy(entry = "LACE") }
        val deleted = input.edit { it.copy(entry = it.entry.dropLast(1), message = "") }
        assertEquals("LAC", deleted.entry)
        assertEquals(listOf("RATE", "CENTRAL", "LATER"), deleted.acceptedOrder)
        assertEquals(3, BeeOpening.progress(board, BeeState.from(deleted.toMap())).words.size)
    }

    @Test fun legacyV1V2SnapshotsStateMapsAndResultFieldsRemainUnchanged() {
        val state = BeeState(setOf("LATER", "RATE"), "CENT", "Saved message")
        assertEquals(mapOf("found" to "LATER,RATE", "entry" to "CENT", "message" to "Saved message"), state.toMap())
        val v1 = original.copy(scoringVersion = BeePuzzle.LEGACY_SCORING)
        val v2 = original.copy(scoringVersion = BeePuzzle.WORD_COUNT_SCORING)
        assertEquals(GameResult(PlayStatus.IN_PROGRESS, "Goal: 6/${v1.target} points · 0 bonus points", 6, v1.maximumGoalScore), BeeRules.result(v1, state))
        assertEquals(GameResult(PlayStatus.IN_PROGRESS, "Daily goal: 2/${v2.wordTarget} words · 6 points", 2, v2.wordTarget), BeeRules.result(v2, state))
        listOf(v1, v2).forEach { legacy ->
            val saved = legacy.snapshot("legacy-${legacy.scoringVersion}")
            assertEquals(saved, BeePuzzle.from(saved).snapshot(saved.id))
            assertEquals(BeeRules.result(legacy, state), BeeRules.result(BeePuzzle.from(saved), BeeState.from(state.toMap())))
            assertNull(BeeRules.submit(legacy, state, "CENTRAL").acceptedOrder)
        }
    }

    @Test fun lateSameOrderAcknowledgementsCannotLoseDraftAfterLatestAcknowledgementOrRestart() {
        val board = board()
        val input = BeeInputBuffer(BeeRules.submit(board, BeeState(), "RATE"))
        val first = input.edit { it.copy(entry = "C") }
        val latest = input.edit { it.copy(entry = "CA") }
        assertEquals("CA", input.acceptExternal(latest).entry)
        assertEquals("CA", input.acceptExternal(first).entry)
        val saved = BeeState.from(input.current.toMap())
        assertEquals(latest.editRevision, saved.editRevision)
        val restarted = BeeInputBuffer(saved)
        val third = restarted.edit { it.copy(entry = "CAR") }
        assertTrue(third.editRevision > latest.editRevision)
        assertEquals("CAR", restarted.acceptExternal(saved).entry)
        assertEquals(listOf("RATE"), restarted.current.acceptedOrder)
    }

    @Test fun initialOpeningDraftAlsoUsesOrderedEditRevisionsBeforeAnyAcceptedWord() {
        val input = BeeInputBuffer(BeeState(), ordered = true)
        input.acceptExternal(BeeState())
        val first = input.edit { it.copy(entry = "C") }
        val latest = input.edit { it.copy(entry = "CA") }
        input.acceptExternal(latest)
        assertEquals("CA", input.acceptExternal(first).entry)
        assertEquals(BeeOpening.STATE, latest.statePolicy)
        assertEquals(emptyList<String>(), latest.acceptedOrder)
        assertEquals(latest, BeeState.from(latest.toMap()))
    }

    @Test fun lateParentAcknowledgementCannotRollBackAcceptedOrderAndConflictingOrderRequiresRecovery() {
        val board = board()
        val input = BeeInputBuffer(BeeState())
        val first = input.edit { BeeRules.submit(board, it, "RATE") }
        input.edit { BeeRules.submit(board, it, "CENTRAL") }
        val latest = input.edit { BeeRules.submit(board, it, "LATER") }
        assertEquals(latest, input.acceptExternal(latest))
        assertEquals(latest, input.acceptExternal(first))
        assertEquals(latest, input.acceptExternal(BeeState()))
        val reorderedMap = latest.toMap() + ("acceptedOrder" to "CENTRAL,RATE,LATER")
        val conflict = input.acceptExternal(BeeState.from(reorderedMap))
        assertTrue(conflict.decodeError.orEmpty().contains("Conflicting"))
        assertEquals(reorderedMap, conflict.toMap())
        assertTrue(BeeRules.result(board, conflict).summary.contains("requires recovery"))
        assertEquals(reorderedMap, BeeRules.submit(board, conflict, "CARE").toMap())
    }

    @Test fun legacyPoliciesRefuseUnknownOrOrderedStateWithoutReinterpretingIt() {
        val futureMap = mapOf("statePolicy" to "future-state", "acceptedOrder" to "RATE", "entry" to "CENT")
        val openingMap = mapOf("statePolicy" to BeeOpening.STATE, "acceptedOrder" to "RATE", "entry" to "CENT")
        listOf(BeePuzzle.LEGACY_SCORING, BeePuzzle.WORD_COUNT_SCORING).forEach { policy ->
            val legacy = original.copy(scoringVersion = policy)
            listOf(futureMap, openingMap).forEach { saved ->
                val state = BeeState.from(saved)
                assertTrue(BeeRules.result(legacy, state).summary.contains("requires recovery"))
                val attempted = BeeRules.submit(legacy, state, "CENTRAL")
                assertEquals(state.found, attempted.found)
                assertEquals(state.acceptedOrder, attempted.acceptedOrder)
                if (state.decodeError != null) assertEquals(saved, attempted.toMap())
            }
        }
    }

    @Test fun rawOpeningSnapshotDuplicateWordsAndWrongRevisionAreRejectedBeforeNormalizedSetsCanHideThem() {
        val snapshot = board().snapshot("raw-opening")
        val invalid = listOf(
            snapshot.copy(revision = 2),
            snapshot.copy(revision = 4),
            snapshot.copy(data = snapshot.data - "scoringVersion"),
            snapshot.copy(data = snapshot.data + ("validWords" to (snapshot.data.getValue("validWords") + ",RATE"))),
            snapshot.copy(data = snapshot.data + ("goalWords" to (snapshot.data.getValue("goalWords") + ",CENTRAL"))),
            snapshot.copy(data = snapshot.data + ("validWords" to (snapshot.data.getValue("validWords") + ",")))
        )
        invalid.forEach { record ->
            val restored = BeePuzzle.from(record)
            assertTrue(restored.snapshotErrors.isNotEmpty())
            assertTrue(BeeRules.validate(restored).isNotEmpty())
            assertTrue(BeeRules.result(restored, BeeState()).summary.contains("requires recovery"))
            assertTrue(BeeRules.submit(restored, BeeState(), "RATE").found.isEmpty())
        }
    }

    @Test fun malformedMissingDuplicateUnknownOrUnsupportedOrderedStateBlocksScoringAndPreservesOriginalMap() {
        val board = board()
        val maps = listOf(
            mapOf("found" to "LATER,RATE", "entry" to "CENT"),
            mapOf("statePolicy" to BeeOpening.STATE, "acceptedOrder" to "RATE,RATE", "entry" to "CENT"),
            mapOf("statePolicy" to BeeOpening.STATE, "acceptedOrder" to "RATE,,LATER"),
            mapOf("statePolicy" to "future-policy", "acceptedOrder" to "RATE"),
            mapOf("statePolicy" to BeeOpening.STATE),
            mapOf("statePolicy" to BeeOpening.STATE, "acceptedOrder" to "RATE,UNKNOWN"),
            mapOf("statePolicy" to BeeOpening.STATE, "acceptedOrder" to "RATE", "found" to "LATER"),
            mapOf("statePolicy" to BeeOpening.STATE, "acceptedOrder" to "RATE", "editRevision" to "-1"),
            mapOf("statePolicy" to BeeOpening.STATE, "acceptedOrder" to "RATE", "editRevision" to "future")
        )
        maps.forEach { saved ->
            val state = BeeState.from(saved)
            assertTrue(saved.toString(), BeeOpening.stateErrors(board, state).isNotEmpty())
            assertTrue(BeeRules.result(board, state).summary.contains("requires recovery"))
            assertNull(BeeRules.result(board, state).score)
            val attempted = BeeRules.submit(board, state, "CENTRAL")
            assertEquals(state.found, attempted.found)
            assertEquals(state.acceptedOrder, attempted.acceptedOrder)
            assertThrows(IllegalArgumentException::class.java) { BeeOpening.progress(board, state) }
            if (state.decodeError != null) assertEquals(saved, attempted.toMap())
        }
    }

    @Test fun inconsistentFrozenBenchmarkThresholdOrUnknownPolicyProducesHonestErrorsWithoutRetrofittingLegacyOrder() {
        val board = board()
        listOf(
            board.copy(openingData = board.openingData + ("benchmarkPoints" to "1")),
            board.copy(openingData = board.openingData + ("benchmarkWords" to "RATE")),
            board.copy(openingData = board.openingData + ("wordTarget" to "1")),
            board.copy(openingData = board.openingData + ("openingLength" to "9")),
            board.copy(openingData = board.openingData + ("statePolicy" to "future-state")),
            board.copy(openingData = board.openingData + ("minimumFamiliarRatioPermille" to "1001")),
            board.copy(openingData = board.openingData - "familiarityVersion"),
            board.copy(scoringVersion = "future-scoring")
        ).forEach { invalid ->
            assertTrue(BeeRules.validate(invalid).isNotEmpty())
            assertTrue(BeeRules.result(invalid, BeeState()).summary.contains("requires recovery"))
            assertTrue(BeeRules.submit(invalid, BeeState(), "RATE").found.isEmpty())
        }
        val lostOrder = BeeState.from(mapOf("found" to "CENTRAL,LATER,RATE"))
        assertTrue(BeeOpening.stateErrors(board, lostOrder).any { it.contains("cannot be inferred") })
        assertNull(lostOrder.acceptedOrder)
    }
}
