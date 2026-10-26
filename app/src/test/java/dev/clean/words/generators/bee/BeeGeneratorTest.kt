package dev.clean.words.generators.bee

import dev.clean.words.core.Attempt
import dev.clean.words.core.Library
import dev.clean.words.core.LibraryCodec
import dev.clean.words.core.PlayStatus
import dev.clean.words.core.PuzzleMode
import dev.clean.words.games.bee.BeeContent
import dev.clean.words.games.bee.BeePuzzle
import dev.clean.words.games.bee.BeeRules
import dev.clean.words.games.bee.BeeState
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class BeeGeneratorTest {
    companion object {
        private fun asset(path: String): String = sequenceOf(File("src/main/assets/$path"), File("app/src/main/assets/$path"))
            .first { it.isFile }.readText(Charsets.UTF_8)
        private val text by lazy { asset(BeeBundledSource.indexPath) }
        private val vocabulary by lazy { BeeVocabulary.fromBundledIndex(text) }
        private val generator by lazy { BeeGenerator(vocabulary) }
        // Independent exhaustive inclusion: no mask index, no index builder, no generator helpers.
        private val sourceDictionary by lazy { asset("dictionaries/enable1.txt").lineSequence()
            .filter { it.matches(Regex("[a-z]{4,32}")) }.map { it.uppercase(java.util.Locale.ROOT) }.toSet() }
        private val tinyProfile = BeeProfile(id = "tiny-test-v1", minFamiliarWords = 1, minAcceptedWords = 1,
            maxAcceptedWords = 100, minFamiliarLengthBuckets = 1, requireFamiliarFour = false)
        private fun tiny() = BeeVocabulary(listOf("CENTRAL", "LATER", "RATE", "TREE"), setOf("CENTRAL", "LATER"), "fixture-v1", "original-fixture-v1")
    }
    private fun success(result: BeeGenerationResult) = result as BeeGenerationResult.Success

    @Test fun packagedIndexMatchesIndependentExhaustiveDictionaryAndPinnedSource() {
        val expected = sourceDictionary.filter { it.toSet().size <= 7 }.sorted()
        assertEquals(expected, vocabulary.words)
        assertEquals(BeeBundledSource.indexWordCount, vocabulary.words.size)
        assertEquals(BeeBundledSource.familiarCount, vocabulary.familiarWords.size)
        assertEquals(BeeBundledSource.dictionarySha256, beeSha256(asset("dictionaries/enable1.txt")))
        assertThrows(IllegalArgumentException::class.java) { BeeVocabulary.fromBundledIndex(text + "BROKEN") }
        assertThrows(BeePreparationCancelled::class.java) { BeeVocabulary.fromBundledIndex(text) { true } }
        var polls = 0
        assertThrows(BeePreparationCancelled::class.java) { BeeVocabulary.fromBundledIndex(text) { ++polls > 200 } }
    }

    @Test fun deterministicSeedCanonicalIdentityAndThresholdSnapshot() {
        val pinned = success(generator.generate(BeeRequest(0)))
        assertEquals("bee:letters:ACGINOR:center:N", pinned.signature)
        assertEquals(205, BeePuzzle.from(pinned.puzzle).validWords.size)
        assertEquals(21, BeePuzzle.from(pinned.puzzle).goalWords.size)
        val request = BeeRequest(Long.MIN_VALUE)
        assertEquals(generator.generate(request), generator.generate(request))
        val generated = success(generator.generate(request))
        assertEquals(generated.signature, generated.puzzle.data["contentSignature"])
        assertEquals(BeeBundledSource.indexSha256, generated.puzzle.data["sourceIndexSha256"])
        assertEquals(BeePuzzle.WORD_COUNT_SCORING, generated.puzzle.data["scoringVersion"])
        assertEquals(2, generated.puzzle.revision)
        assertEquals(BeeGenerator.signature("CENTRAL", 'A'), BeeGenerator.signature("TRANELC", 'A'))
        val board = BeePuzzle.from(generated.puzzle)
        assertEquals(board.wordTarget.toString(), generated.puzzle.data["wordTarget"])
        assertEquals((board.goalWords.size * 7 + 9) / 10, board.wordTarget)
        assertTrue((0L..30L).map { success(generator.generate(BeeRequest(it))).signature }.distinct().size > 10)
    }

    @Test fun emptyTinyMalformedAndBoundedSearchHaveExplicitResults() {
        val empty = BeeVocabulary(emptyList(), emptySet(), "fixture", "fixture")
        assertTrue(BeeGenerator(empty).generate(BeeRequest(0, tinyProfile)) is BeeGenerationResult.Exhausted)
        val tiny = BeeGenerator(tiny())
        val generated = success(tiny.generate(BeeRequest(0, tinyProfile)))
        assertEquals(emptyList<String>(), BeeRules.validate(BeePuzzle.from(generated.puzzle)))
        val rejected = tiny.generate(BeeRequest(0, BeeProfile(maxCandidateAttempts = 1))) as BeeGenerationResult.SearchLimit
        assertEquals(1, rejected.diagnostics.evaluated)
        assertTrue(rejected.diagnostics.rejections.isNotEmpty())
        assertTrue(tiny.generate(BeeRequest(0, tinyProfile.copy(maxCandidateAttempts = 7, minFamiliarWords = 99))) is BeeGenerationResult.Exhausted)
        assertTrue(tiny.generate(BeeRequest(0, tinyProfile.copy(maxCandidateAttempts = 0))) is BeeGenerationResult.InvalidInput)
        listOf(listOf("central"), listOf("CENTRAL", "CENTRAL"), listOf("ABC"), listOf("ABCDEFGHI")).forEach { words ->
            assertThrows(IllegalArgumentException::class.java) { BeeVocabulary(words, emptySet(), "fixture", "fixture") }
        }
        assertThrows(IllegalArgumentException::class.java) { BeeVocabulary(listOf("RATE"), setOf("LATER"), "fixture", "fixture") }
    }

    @Test fun cancellationAndExclusionNeverRelaxQualityOrCallShufflesNovel() {
        val tiny = BeeGenerator(tiny())
        assertEquals(BeeGenerationResult.Cancelled(0), tiny.generate(BeeRequest(0, tinyProfile)) { true })
        var polls = 0
        assertTrue(tiny.generate(BeeRequest(0, tinyProfile)) { ++polls > 12 } is BeeGenerationResult.Cancelled)
        val all = "ACELNRT".map { BeeGenerator.signature("CENTRAL", it) }.toSet()
        val drained = tiny.generate(BeeRequest(0, tinyProfile, all)) as BeeGenerationResult.Exhausted
        assertEquals(7, drained.diagnostics.excluded)
        assertEquals(0, drained.diagnostics.evaluated)
        val leaveOnlyA = all - BeeGenerator.signature("CENTRAL", 'A')
        assertEquals("bee:letters:ACELNRT:center:A", success(tiny.generate(BeeRequest(0, tinyProfile, leaveOnlyA))).signature)
        val first = success(generator.generate(BeeRequest(0)))
        val next = success(generator.generate(BeeRequest(0, recentSignatures = setOf(first.signature))))
        assertNotEquals(first.signature, next.signature)
    }

    @Test fun generatedScoresPangramsLengthBarsDuplicateHandlingAndEqualCreditAreIndependent() {
        val generated = success(generator.generate(BeeRequest(23)))
        val board = BeePuzzle.from(generated.puzzle)
        val expectedPangrams = board.validWords.filter { it.toSet() == board.letters.toSet() }.sorted()
        assertEquals(expectedPangrams.joinToString(","), generated.puzzle.data["pangrams"])
        val all = BeeState(board.validWords + "INVALID", entry = "DRAFT")
        val score = board.validWords.sumOf { word -> (if (word.length == 4) 1 else word.length) +
            if (word.toSet() == board.letters.toSet()) 7 else 0 }
        assertEquals(score, BeeRules.totalScore(board, all))
        val buckets = board.validWords.groupBy { it.length }.toSortedMap()
        assertEquals(buckets.map { (length, words) -> Triple(length, words.size, words.size) },
            BeeRules.lengthProgress(board, all).map { Triple(it.length, it.found, it.total) })
        assertEquals(buckets.entries.joinToString(",") { "${it.key}:${it.value.size}" }, generated.puzzle.data["lengthTotals"])
        val nonFamiliar = board.validWords.filter { it !in board.goalWords }.take(board.wordTarget)
        assertEquals(board.wordTarget, nonFamiliar.size)
        var state = BeeState()
        nonFamiliar.forEach { word ->
            val before = state.found.size
            state = BeeRules.submit(board, state, word.lowercase())
            assertEquals(before + 1, state.found.size)
            assertEquals(state.found, BeeRules.submit(board, state, word).found)
        }
        assertEquals(PlayStatus.COMPLETED, BeeRules.result(board, state).status)
        assertEquals(board.wordTarget, BeeRules.result(board, state).score)
        assertTrue(board.goalWords.intersect(state.found).isEmpty())
        assertEquals(PlayStatus.COMPLETED, BeeRules.result(board, BeeState(board.goalWords)).status)
    }

    @Test fun sourcesCopyAndOldV1V2SnapshotsKeepTheirResultsAcrossNewBanks() {
        val words = mutableListOf("CENTRAL", "LATER", "RATE", "TREE")
        val familiar = mutableSetOf("CENTRAL", "LATER")
        val source = BeeVocabulary(words, familiar, "fixture-v1", "fixture-v1")
        val local = BeeGenerator(source)
        val saved = success(local.generate(BeeRequest(0, tinyProfile))).puzzle
        words.clear(); familiar.clear()
        assertEquals(saved, success(local.generate(BeeRequest(0, tinyProfile))).puzzle)
        val old = BeeContent.puzzles.map { it.copy(data = it.data.toMap()) }
        val v2 = BeePuzzle.from(old.first())
        val v1 = v2.copy(scoringVersion = BeePuzzle.LEGACY_SCORING).snapshot("old-v1")
        val state = BeeState(setOf(v2.validWords.first { it !in v2.goalWords }), "CENT")
        val legacyResult = BeeRules.result(BeePuzzle.from(v1), state)
        val currentResult = BeeRules.result(v2, state)
        repeat(40) { generator.generate(BeeRequest(it.toLong())) }
        assertEquals(old, BeeContent.puzzles)
        assertEquals(legacyResult, BeeRules.result(BeePuzzle.from(v1), state))
        assertEquals(currentResult, BeeRules.result(v2, state))
        val time = "2026-10-25T12:00:00Z"
        val library = Library(listOf(Attempt("frozen", "bee", PuzzleMode.EXTRA, null, saved, state.toMap(),
            startedAt = time, updatedAt = time, result = BeeRules.result(BeePuzzle.from(saved), state))))
        assertEquals(library, LibraryCodec.decode(LibraryCodec.encode(library)))
        assertEquals("CENT", BeeState.from(LibraryCodec.decode(LibraryCodec.encode(library)).attempts.single().state).entry)
    }

    @Test fun thousandSeedBenchmarkAndIndependentExhaustiveAnswers() {
        val preparationStart = System.nanoTime()
        BeeVocabulary.fromBundledIndex(text)
        val preparationMicros = (System.nanoTime() - preparationStart) / 1000
        repeat(40) { generator.generate(BeeRequest(it.toLong())) }
        val times = mutableListOf<Long>()
        val signatures = mutableSetOf<String>()
        val letterSets = mutableSetOf<String>()
        val acceptedCounts = mutableListOf<Int>()
        val familiarCounts = mutableListOf<Int>()
        val goals = mutableListOf<Int>()
        val rejections = mutableMapOf<String, Int>()
        var worstInspected = 0
        repeat(1000) { seed ->
            val start = System.nanoTime()
            val result = success(generator.generate(BeeRequest(seed.toLong())))
            times += System.nanoTime() - start
            val board = BeePuzzle.from(result.puzzle)
            worstInspected = maxOf(worstInspected, result.diagnostics.inspected)
            result.diagnostics.rejections.forEach { (reason, count) -> rejections[reason] = (rejections[reason] ?: 0) + count }
            letterSets += board.letters
            acceptedCounts += board.validWords.size
            familiarCounts += board.goalWords.size
            goals += board.wordTarget
            assertTrue(board.wordTarget <= board.goalWords.size)
            assertEquals(emptyList<String>(), BeeRules.validate(board))
            if (signatures.add(result.signature)) {
                val expected = sourceDictionary.filter { word -> board.center in word && word.all { it in board.letters } }.toSet()
                assertEquals("Seed $seed ${result.signature}", expected, board.validWords)
                assertEquals(expected.intersect(vocabulary.familiarWords), board.goalWords)
            }
        }
        fun distribution(values: List<Int>): String {
            val sorted = values.sorted(); return "${sorted.first()}/${sorted[499]}/${sorted[949]}/${sorted.last()}"
        }
        times.sort()
        println("BEE_BENCHMARK trials=1000 distinctSetCenters=${signatures.size} distinctSets=${letterSets.size} failures=0 goalUnattainable=0 indexedWords=${vocabulary.words.size} candidateSets=${vocabulary.candidateMasks.size} preparationUs=$preparationMicros worstInspected=$worstInspected p50us=${times[499]/1000} p95us=${times[949]/1000} maxus=${times.last()/1000} acceptedMinP50P95Max=${distribution(acceptedCounts)} familiarMinP50P95Max=${distribution(familiarCounts)} goalMinP50P95Max=${distribution(goals)} rejections=${rejections.toSortedMap()}")
        assertTrue(signatures.size > 400)
        assertTrue(letterSets.size > 100)
    }
}
