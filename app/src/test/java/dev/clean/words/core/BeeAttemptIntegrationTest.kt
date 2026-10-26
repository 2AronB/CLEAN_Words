package dev.clean.words.core

import dev.clean.words.games.bee.BeeContent
import dev.clean.words.games.bee.BeeGame
import dev.clean.words.games.bee.BeeOpening
import dev.clean.words.games.bee.BeeOpeningQuality
import dev.clean.words.games.bee.BeePuzzle
import dev.clean.words.games.bee.BeeRules
import dev.clean.words.games.bee.BeeState
import dev.clean.words.generators.bee.BeeBundledSource
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class BeeAttemptIntegrationTest {
    private val start = Instant.parse("2026-10-26T12:00:00Z")
    private val zone = ZoneId.of("UTC")
    private val original = BeePuzzle.from(BeeContent.puzzles.first())
    private val pins = mapOf(
        "generator" to "fixture-generator-v3", "profile" to "fixture-profile-v3",
        "profileThresholds" to "fixture-thresholds", "sourceIndex" to "fixture-index",
        "sourceIndexSha256" to "fixture-hash", "familiarityVersion" to "fixture-familiar-v1",
    )

    private fun board(familiarCount: Int): BeePuzzle {
        val familiar = (listOf("CENTRAL") + original.goalWords.sorted().filter { it != "CENTRAL" })
            .take(familiarCount).toSet()
        return BeeOpening.freeze(original.copy(goalWords = familiar), pins, BeeOpeningQuality(0, "test-only"))
    }

    private fun attempt(board: BeePuzzle, mode: PuzzleMode = PuzzleMode.DAILY): Attempt =
        AttemptRules.createSnapshot(Library(), BeeGame, mode, board.snapshot("bee-opening-fixture"), start, zone, "fixture-attempt")

    private fun accepted(board: BeePuzzle, count: Int): List<String> = board.validWords.sortedDescending().take(count)

    private fun advance(attempt: Attempt, words: List<String>): Attempt {
        val board = BeePuzzle.from(attempt.puzzle)
        val resumedAt = Instant.parse(attempt.updatedAt)
        return words.foldIndexed(attempt) { index, current, word ->
            val next = BeeRules.submit(board, BeeState.from(current.state), word)
            AttemptRules.update(current, next.toMap(), BeeGame, resumedAt.plusSeconds((index + 1).toLong()))
        }
    }

    @Test fun goalReachedBeforeTenKeepsOpeningResumableAndPreservesGoalTimestamp() {
        val b = board(10)
        assertEquals(7, b.wordTarget)
        val order = accepted(b, 12)
        val reached = advance(attempt(b), order.take(7))
        val progress = AttemptPresentation.inspect(reached)
        assertEquals(PlayStatus.COMPLETED, reached.result.status)
        assertEquals(start.plusSeconds(7).toString(), reached.completedAt)
        assertTrue(progress.needsResume)
        assertEquals("…", progress.calendarMark)
        assertTrue(progress.label.contains("opening unfinished (7/10)"))
        assertTrue(progress.openingLabel!!.contains("provisional (7/10)"))
        assertTrue(progress.goalLabel!!.contains("7/7 · reached"))
        assertFalse(progress.opening!!.recorded)
        val tomorrow = LocalDate.parse(reached.puzzleDate).plusDays(1)
        assertEquals(reached, AttemptRules.previousUnfinished(Library(listOf(reached)), "bee", tomorrow))

        val recorded = advance(reached, order.drop(7).take(3))
        assertEquals(reached.completedAt, recorded.completedAt)
        assertFalse(AttemptPresentation.inspect(recorded).needsResume)
        assertEquals("✓", AttemptPresentation.inspect(recorded).calendarMark)
        assertTrue(AttemptPresentation.inspect(recorded).openingLabel!!.contains("recorded"))
        assertNull(AttemptRules.previousUnfinished(Library(listOf(recorded)), "bee", tomorrow))
        assertNull(recorded.result.score)
        assertNull(recorded.result.maxScore)
    }

    @Test fun tenBeforeGoalRecordsScoreWithoutPrematureCompletionOrFinishTimestamp() {
        val b = board(20)
        assertEquals(14, b.wordTarget)
        val order = accepted(b, 16)
        val opening = advance(attempt(b), order.take(10))
        val progress = AttemptPresentation.inspect(opening)
        val openingProgress = requireNotNull(progress.opening)
        assertTrue(openingProgress.recorded)
        assertTrue(progress.needsResume)
        assertEquals("…", progress.calendarMark)
        assertEquals("Opening recorded · word goal unfinished", progress.label)
        assertEquals(PlayStatus.IN_PROGRESS, opening.result.status)
        assertNull(opening.completedAt)
        assertNull(opening.result.score)
        assertNull(opening.result.maxScore)
        val tomorrow = LocalDate.parse(opening.puzzleDate).plusDays(1)
        assertEquals(opening, AttemptRules.previousUnfinished(Library(listOf(opening)), "bee", tomorrow))

        val complete = advance(opening, order.drop(10).take(4))
        val completeProgress = AttemptPresentation.inspect(complete)
        val completeOpening = requireNotNull(completeProgress.opening)
        assertFalse(completeProgress.needsResume)
        assertEquals("✓", completeProgress.calendarMark)
        assertEquals(PlayStatus.COMPLETED, complete.result.status)
        assertEquals(openingProgress.percent, completeOpening.percent)
        assertEquals(openingProgress.words, completeOpening.words)
        assertNull(AttemptRules.previousUnfinished(Library(listOf(complete)), "bee", tomorrow))
        val explored = advance(complete, order.drop(14))
        assertEquals(complete.completedAt, explored.completedAt)
        assertEquals(openingProgress.percent, AttemptPresentation.inspect(explored).opening!!.percent)
        assertTrue(AttemptPresentation.inspect(explored).opening!!.allWordPoints > completeOpening.allWordPoints)
    }

    @Test fun openingAndGoalCanFinishTogetherWithoutDuplicatingOrLosingMilestones() {
        val b = board(14)
        assertEquals(10, b.wordTarget)
        val nine = advance(attempt(b), accepted(b, 9))
        assertTrue(AttemptPresentation.inspect(nine).needsResume)
        assertEquals(PlayStatus.IN_PROGRESS, nine.result.status)
        assertNull(nine.completedAt)
        val tenth = accepted(b, 10).last()
        val complete = advance(nine, listOf(tenth))
        assertEquals(PlayStatus.COMPLETED, complete.result.status)
        assertFalse(AttemptPresentation.inspect(complete).needsResume)
        assertEquals("Opening recorded · word goal reached", AttemptPresentation.inspect(complete).label)
        assertEquals(10, AttemptPresentation.inspect(complete).opening!!.words.size)
        val duplicated = BeeRules.submit(b, BeeState.from(complete.state), tenth)
        val same = AttemptRules.update(complete, duplicated.toMap(), BeeGame, start.plusSeconds(100))
        assertEquals(complete.completedAt, same.completedAt)
        assertEquals(AttemptPresentation.inspect(complete), AttemptPresentation.inspect(same))
    }

    @Test fun codecRetainsNonAlphabeticOrderAndGoalCompleteOpeningPendingAcrossRestart() {
        val b = board(10)
        val order = accepted(b, 7)
        assertNotEquals(order.sorted(), order)
        val saved = advance(attempt(b), order)
        val library = Library(listOf(saved))
        val restored = LibraryCodec.decode(LibraryCodec.encode(library))
        assertEquals(library, restored)
        val reopened = restored.attempts.single()
        assertEquals(order, BeeState.from(reopened.state).acceptedOrder)
        assertEquals(PlayStatus.COMPLETED, reopened.result.status)
        assertTrue(AttemptPresentation.inspect(reopened).needsResume)
        assertEquals(AttemptPresentation.inspect(saved), AttemptPresentation.inspect(reopened))
        assertEquals(saved.puzzleDate, reopened.puzzleDate)
        assertEquals(saved.completedAt, reopened.completedAt)
    }

    @Test fun malformedProposedOrderCannotReplaceSavedOrderOrResult() {
        val b = board(20)
        val saved = advance(attempt(b), listOf("RATE", "CENTRAL"))
        val before = LibraryCodec.encode(Library(listOf(saved)))
        val proposals = listOf(
            saved.state + ("acceptedOrder" to "RATE,RATE"),
            saved.state + ("acceptedOrder" to "RATE,UNKNOWN"),
            saved.state + ("statePolicy" to "future-state"),
            saved.state - "acceptedOrder",
        )
        proposals.forEach { proposed ->
            assertThrows(IllegalArgumentException::class.java) {
                AttemptRules.update(saved, proposed, BeeGame, start.plusSeconds(10))
            }
            assertArrayEquals(before, LibraryCodec.encode(Library(listOf(saved))))
        }
    }

    @Test fun existingMalformedOpeningIsRecoveryOnlyEvenWhenOfferedCleanReplacement() {
        val b = board(20)
        val valid = advance(attempt(b), listOf("RATE", "CENTRAL"))
        val raw = valid.state + ("acceptedOrder" to "RATE,RATE")
        val damaged = valid.copy(state = raw)
        val presentation = AttemptPresentation.inspect(damaged)
        assertTrue(presentation.errors.isNotEmpty())
        assertNull(presentation.opening)
        assertEquals("!", presentation.calendarMark)
        assertTrue(presentation.needsResume)
        val restored = LibraryCodec.decode(LibraryCodec.encode(Library(listOf(damaged)))).attempts.single()
        assertEquals(raw, restored.state)
        assertEquals(valid.result, restored.result)
        assertThrows(IllegalArgumentException::class.java) {
            AttemptRules.update(restored, valid.state, BeeGame, start.plusSeconds(10))
        }
        assertEquals(raw, restored.state)
        assertEquals(valid.result, restored.result)
    }

    @Test fun repositoryRejectsAcceptedOrderRewritesAndStaleOrConflictingEditRevisions() {
        val b = board(20)
        val initial = advance(attempt(b), listOf("RATE", "CENTRAL", "LATER"))
        val positive = BeeState.from(initial.state).copy(entry = "CA", editRevision = 7).toMap()
        val saved = AttemptRules.update(initial, positive, BeeGame, start.plusSeconds(10))
        val invalid = listOf(
            positive + ("acceptedOrder" to "RATE,CENTRAL"),
            positive + ("acceptedOrder" to "CENTRAL,RATE,LATER"),
            positive + ("acceptedOrder" to "RATE,CENTRAL,LATER,CARE") + ("editRevision" to "6"),
            positive + ("entry" to "CAR"),
        )
        val bytes = LibraryCodec.encode(Library(listOf(saved)))
        invalid.forEach { proposed ->
            assertThrows(IllegalArgumentException::class.java) {
                AttemptRules.update(saved, proposed, BeeGame, start.plusSeconds(11))
            }
            assertArrayEquals(bytes, LibraryCodec.encode(Library(listOf(saved))))
        }
        val duplicateAcknowledgement = AttemptRules.update(saved, positive, BeeGame, start.plusSeconds(11))
        assertEquals(saved.state, duplicateAcknowledgement.state)
        val next = positive + ("acceptedOrder" to "RATE,CENTRAL,LATER,CARE") + ("entry" to "") + ("editRevision" to "8")
        val accepted = AttemptRules.update(saved, next, BeeGame, start.plusSeconds(12))
        assertEquals(listOf("RATE", "CENTRAL", "LATER", "CARE"), BeeState.from(accepted.state).acceptedOrder)
        assertEquals(8L, BeeState.from(accepted.state).editRevision)
    }

    @Test fun legacySnapshotsRetainScoreCompletionAndNoInventedOpening() {
        listOf(BeePuzzle.LEGACY_SCORING, BeePuzzle.WORD_COUNT_SCORING).forEach { policy ->
            val legacy = original.copy(scoringVersion = policy)
            val record = legacy.snapshot("legacy-$policy")
            val startAttempt = AttemptRules.createSnapshot(Library(), BeeGame, PuzzleMode.DAILY, record, start, zone)
            assertNull(AttemptPresentation.inspect(startAttempt).opening)
            assertTrue(AttemptPresentation.inspect(startAttempt).needsResume)
            val state = legacy.goalWords.fold(BeeState()) { s, word -> BeeRules.submit(legacy, s, word) }
            assertNull(state.acceptedOrder)
            val complete = AttemptRules.update(startAttempt, state.toMap(), BeeGame, start.plusSeconds(1))
            assertEquals(BeeRules.result(legacy, state), complete.result)
            assertNotNull(complete.result.score)
            assertNotNull(complete.result.maxScore)
            assertFalse(AttemptPresentation.inspect(complete).needsResume)
            assertEquals("Completed", AttemptPresentation.inspect(complete).label)
            assertNull(AttemptPresentation.inspect(complete).openingLabel)
            assertEquals(record, complete.puzzle)
            assertEquals(complete, LibraryCodec.decode(LibraryCodec.encode(Library(listOf(complete)))).attempts.single())
        }
    }

    @Test fun activeAdapterUsesApprovedZeroFilterV3AndFullUnchangedAcceptedVocabulary() {
        fun asset(name: String) = listOf(File("src/main/assets/$name"), File("app/src/main/assets/$name"))
            .first { it.exists() }.readText(Charsets.UTF_8)
        val engine = OfflinePuzzleGeneration(::asset)
        val date = LocalDate.of(2026, 10, 26)
        val request = GenerationRequest("bee", PuzzleMode.DAILY, date,
            GenerationSeeds.daily("bee", engine.policy("bee"), date), "real-daily", engine.policy("bee"))
        val record = (engine.generate(request) { false } as GenerationOutcome.Ready).puzzle
        val b = BeePuzzle.from(record)
        assertEquals(3, record.revision)
        assertEquals(BeeOpening.SCORING, b.scoringVersion)
        assertEquals("0", record.data["minimumFamiliarRatioPermille"])
        assertEquals(OfflinePuzzleGeneration.beeOpeningApproval, record.data["qualityApprovalVersion"])
        assertEquals(BeeBundledSource.indexSha256, record.data["sourceIndexSha256"])
        assertEquals(BeeBundledSource.familiarityVersion, record.data["familiarityVersion"])
        assertEquals(emptyList<String>(), BeeRules.validate(b))
        assertEquals(record, (engine.generate(request) { false } as GenerationOutcome.Ready).puzzle)

        val expected = asset("dictionaries/enable1.txt").lineSequence().map { it.trim() }
            .filter { it.matches(Regex("[a-z]{4,32}")) }.map { it.uppercase() }
            .filter { b.center in it && it.all { letter -> letter in b.letters } }.toSet()
        assertEquals(expected, b.validWords)
        val familiar = asset(BeeBundledSource.indexPath).lineSequence().drop(1).filter { it.isNotEmpty() }
            .map { it.split('\t') }.filter { it[2] == "1" }.map { it[1] }.toSet()
        assertEquals(expected.intersect(familiar), b.goalWords)

        val priorV2 = AttemptRules.createSnapshot(Library(), BeeGame, PuzzleMode.DAILY,
            BeeContent.puzzles.first(), start, zone, "old-daily")
        val reopened = AttemptRules.createSnapshot(Library(listOf(priorV2)), BeeGame, PuzzleMode.DAILY,
            record, start.plusSeconds(30), zone, "replacement-daily")
        assertEquals(priorV2, reopened)
        assertEquals(BeePuzzle.WORD_COUNT_SCORING, reopened.puzzle.data["scoringVersion"])
    }
}
