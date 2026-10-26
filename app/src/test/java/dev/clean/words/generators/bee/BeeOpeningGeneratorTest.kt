package dev.clean.words.generators.bee

import dev.clean.words.games.bee.BeeOpening
import dev.clean.words.games.bee.BeeOpeningQuality
import dev.clean.words.games.bee.BeePuzzle
import dev.clean.words.games.bee.BeeRules
import dev.clean.words.games.bee.BeeState
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class BeeOpeningGeneratorTest {
    private val words = listOf("CENTRAL", "LATER", "RATE", "CARE", "LACE", "REAL", "TEAR", "AREA", "RACE", "LATE", "ALERT", "ALTER", "CATER", "CRATE", "CRANE", "CREATE", "CENTRALCENTRAL")
    private val familiar = words.dropLast(1).toSet()
    private val profile = BeeProfile("fixture-opening-v1", minFamiliarWords = 1, minAcceptedWords = 1,
        maxAcceptedWords = 100, minFamiliarLengthBuckets = 1, requireFamiliarFour = false, maxCandidateAttempts = 7)
    // Fixture-only choices do not activate any production quality policy.
    private val fixtureQuality = BeeOpeningQuality(0, "test-fixture-only-v1")
    private fun vocabulary() = BeeVocabulary(words, familiar, "fixture-dictionary-v1", "fixture-editorial-v1")
    private fun success(result: BeeGenerationResult) = result as BeeGenerationResult.Success

    @Test fun optInNeedsAnExplicitVersionedDecisionAndLegacyGenerationStaysV2() {
        val generator = BeeGenerator(vocabulary())
        assertTrue(generator.generateOpening(BeeRequest(0, profile), null) is BeeGenerationResult.InvalidInput)
        assertThrows(IllegalArgumentException::class.java) { BeeOpeningQuality(600, "") }
        assertThrows(IllegalArgumentException::class.java) { BeeOpeningQuality(1001, "fixture") }
        val old = success(generator.generate(BeeRequest(0, profile)))
        assertEquals(2, old.puzzle.revision)
        assertEquals(BeePuzzle.WORD_COUNT_SCORING, old.puzzle.data["scoringVersion"])
        assertFalse(old.puzzle.data.containsKey("benchmarkPoints"))
        val opening = success(generator.generateOpening(BeeRequest(0, profile), fixtureQuality))
        assertNotEquals(old.puzzle.id, opening.puzzle.id)
    }

    @Test fun openingSelectionIsDeterministicAndFreezesAllPolicyAndBenchmarkInputs() {
        val generator = BeeGenerator(vocabulary())
        val request = BeeRequest(17, profile)
        val generated = success(generator.generateOpening(request, fixtureQuality))
        assertEquals(generated, generator.generateOpening(request, fixtureQuality))
        val board = BeePuzzle.from(generated.puzzle)
        assertEquals(3, generated.puzzle.revision)
        assertEquals(BeeOpening.SCORING, board.scoringVersion)
        assertEquals(emptyList<String>(), BeeRules.validate(board))
        assertEquals(generated.puzzle, board.snapshot(generated.puzzle.id))
        assertEquals("10", generated.puzzle.data["openingLength"])
        assertEquals(BeeGenerator.openingVersion, generated.puzzle.data["generator"])
        assertEquals(vocabulary().familiarityVersion, generated.puzzle.data["familiarityVersion"])
        assertEquals("test-fixture-only-v1", generated.puzzle.data["qualityApprovalVersion"])
        assertEquals("0", generated.puzzle.data["minimumFamiliarRatioPermille"])
        assertEquals(BeeOpening.benchmark(board).toString(), generated.puzzle.data["benchmarkPoints"])
        assertEquals(BeeOpening.bestTen(board).joinToString(","), generated.puzzle.data["benchmarkWords"])
        assertTrue(generated.puzzle.data.getValue("profileThresholds").contains(BeeOpening.SCORING))
        val changedApproval = success(generator.generateOpening(request, fixtureQuality.copy(approvalVersion = "test-fixture-only-v2")))
        assertNotEquals(generated.puzzle.id, changedApproval.puzzle.id)
    }

    @Test fun strictQualityExhaustionAndSearchLimitCannotReturnAnUnqualifiedFallback() {
        val generator = BeeGenerator(vocabulary())
        val strict = BeeOpeningQuality(1000, "fixture-requires-perfect-v1")
        val exhausted = generator.generateOpening(BeeRequest(0, profile), strict) as BeeGenerationResult.Exhausted
        assertEquals(7, exhausted.diagnostics.evaluated)
        assertTrue(exhausted.diagnostics.rejections.getValue("opening-familiar-ratio") > 0)
        assertTrue(generator.generateOpening(BeeRequest(0, profile.copy(maxCandidateAttempts = 1)), strict) is BeeGenerationResult.SearchLimit)
        assertTrue(generator.generateOpening(BeeRequest(0, profile.copy(maxCandidateAttempts = 0)), strict) is BeeGenerationResult.InvalidInput)
        assertTrue(generator.generateOpening(BeeRequest(0, profile.copy(minFamiliarWords = 99)), fixtureQuality) is BeeGenerationResult.Exhausted)
    }

    @Test fun cancellationAndSemanticExclusionsStillApplyToOpeningGeneration() {
        val generator = BeeGenerator(vocabulary())
        val request = BeeRequest(9, profile)
        assertEquals(BeeGenerationResult.Cancelled(0), generator.generateOpening(request, fixtureQuality) { true })
        var polls = 0
        assertTrue(generator.generateOpening(request, fixtureQuality) { ++polls > 30 } is BeeGenerationResult.Cancelled)
        val generated = success(generator.generateOpening(request, fixtureQuality))
        val all = "ACELNRT".map { BeeGenerator.signature("ACELNRT", it) }.toSet()
        val drained = generator.generateOpening(request.copy(recentSignatures = all), fixtureQuality) as BeeGenerationResult.Exhausted
        assertEquals(7, drained.diagnostics.excluded)
        assertEquals(0, drained.diagnostics.evaluated)
        val excludingFirst = generator.generateOpening(request.copy(recentSignatures = setOf(generated.signature)), fixtureQuality)
        if (excludingFirst is BeeGenerationResult.Success) assertNotEquals(generated.signature, excludingFirst.signature)
    }

    @Test fun savedOpeningDoesNotConsultChangedVocabularyOrFamiliarityInput() {
        val mutableWords = words.toMutableList()
        val mutableFamiliar = familiar.toMutableSet()
        val source = BeeVocabulary(mutableWords, mutableFamiliar, "source-v1", "editorial-v1")
        val generator = BeeGenerator(source)
        val saved = success(generator.generateOpening(BeeRequest(3, profile), fixtureQuality)).puzzle
        val board = BeePuzzle.from(saved)
        var state = BeeState()
        BeeOpening.bestTen(board).forEach { state = BeeRules.submit(board, state, it) }
        val progress = BeeOpening.progress(board, state)
        mutableWords.clear(); mutableFamiliar.clear()
        val replacement = BeeVocabulary(words, words.toSet(), "source-v2", "frequency-backed-fixture-v2")
        val changed = success(BeeGenerator(replacement).generateOpening(BeeRequest(3, profile), fixtureQuality)).puzzle
        assertNotEquals(saved.id, changed.id)
        assertEquals(progress, BeeOpening.progress(BeePuzzle.from(saved), BeeState.from(state.toMap())))
        assertEquals(saved, success(generator.generateOpening(BeeRequest(3, profile), fixtureQuality)).puzzle)
    }

    @Test fun realCorpusOpeningRequestsRespectExactRatioWithoutRelaxation() {
        val path = BeeBundledSource.indexPath
        val text = sequenceOf(File("src/main/assets/$path"), File("app/src/main/assets/$path")).first { it.isFile }.readText(Charsets.UTF_8)
        val generator = BeeGenerator(BeeVocabulary.fromBundledIndex(text))
        val evaluatedPolicy = BeeOpeningQuality(600, "test-evaluation-only-not-owner-approval")
        repeat(30) { seed ->
            val generated = success(generator.generateOpening(BeeRequest(seed.toLong()), evaluatedPolicy))
            val board = BeePuzzle.from(generated.puzzle)
            val absolute = board.validWords.sortedByDescending { BeeRules.points(board, it) }.take(10).sumOf { BeeRules.points(board, it) }
            val familiar = board.goalWords.sortedByDescending { BeeRules.points(board, it) }.take(10).sumOf { BeeRules.points(board, it) }
            assertTrue(familiar.toLong() * 1000 >= absolute.toLong() * 600)
            assertEquals(absolute.toString(), generated.puzzle.data["benchmarkPoints"])
            assertEquals(emptyList<String>(), BeeRules.validate(board))
        }
    }
}
