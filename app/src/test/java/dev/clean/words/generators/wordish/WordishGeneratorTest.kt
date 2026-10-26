package dev.clean.words.generators.wordish

import dev.clean.words.core.PlayStatus
import dev.clean.words.core.Attempt
import dev.clean.words.core.Library
import dev.clean.words.core.LibraryCodec
import dev.clean.words.core.PuzzleMode
import dev.clean.words.games.wordish.LetterFeedback
import dev.clean.words.games.wordish.WordishGame
import dev.clean.words.games.wordish.WordishRules
import org.junit.Assert.*
import org.junit.Test

class WordishGeneratorTest {
    private fun source(vararg words: String) = WordishAnswerSource(words.toList(), "fixture-v1", "Original test fixture")
    private fun success(result: WordishGenerationResult) = result as WordishGenerationResult.Success

    @Test fun reproductionAndCanonicalBankOrder() {
        val a = WordishGenerator(source("APPLE", "CRANE", "ABBEY"))
        val b = WordishGenerator(source("ABBEY", "APPLE", "CRANE"))
        val request = WordishRequest(Long.MIN_VALUE, recentSignatures = setOf(WordishGenerator.answerSignature("CRANE")))
        assertEquals(a.generate(request), b.generate(request))
        assertEquals(a.generate(request), a.generate(request))
        assertEquals("PUPPY", success(WordishGenerator().generate(WordishRequest(0))).puzzle.data["answer"])
        assertTrue((0L..40).map { success(a.generate(WordishRequest(it))).signature }.distinct().size > 1)
    }

    @Test fun tinyExhaustedAndUnknownPolicyAreExplicit() {
        assertTrue(WordishGenerator(source()).generate(WordishRequest(1)) is WordishGenerationResult.Exhausted)
        val g = WordishGenerator(source("APPLE"))
        assertEquals("APPLE", success(g.generate(WordishRequest(1))).puzzle.data["answer"])
        val exhausted = g.generate(WordishRequest(2, recentSignatures = setOf("wordish:answer:APPLE"))) as WordishGenerationResult.Exhausted
        assertEquals(WordishDiagnostics(1, 1, 1, 0), exhausted.diagnostics)
        assertTrue(g.generate(WordishRequest(1, dictionaryPolicy = "unknown")) is WordishGenerationResult.InvalidInput)
    }

    @Test fun malformedBanksNeverSilentlyBecomeFairAnswers() {
        listOf(source("APPLE", "APPLE"), source("apple"), source("ABCD"), source("ZZZZZ"),
            WordishAnswerSource(listOf("APPLE"), "", "fixture")).forEach {
            assertTrue(WordishGenerator(it).generate(WordishRequest(0)) is WordishGenerationResult.InvalidInput)
        }
    }

    @Test fun cancellationOccursBeforeAndDuringScan() {
        val g = WordishGenerator(source("ABBEY", "APPLE", "CRANE"))
        assertEquals(WordishGenerationResult.Cancelled(0), g.generate(WordishRequest(1)) { true })
        var polls = 0
        assertEquals(WordishGenerationResult.Cancelled(1), g.generate(WordishRequest(1)) { ++polls == 3 })
        assertTrue(g.generate(WordishRequest(1)) is WordishGenerationResult.Success)
    }

    @Test fun allEligibleAnswersArePlayableAndRecentWindowCanDrainBank() {
        val bank = WordishGenerator.bundledSource()
        assertTrue(bank.words.size >= 500)
        assertEquals(bank.words.size, bank.words.distinct().size)
        bank.words.forEach { assertTrue(it, WordishRules.isAccepted(it)) }
        val recent = mutableSetOf<String>()
        val g = WordishGenerator()
        repeat(bank.words.size) { seed ->
            val result = success(g.generate(WordishRequest(seed.toLong(), recentSignatures = recent)))
            assertTrue(recent.add(result.signature))
            assertEquals(emptyList<String>(), WordishRules.validate(result.puzzle))
            val solved = WordishRules.submit(result.puzzle, mapOf("draft" to result.puzzle.data.getValue("answer")))
            assertEquals(PlayStatus.COMPLETED, WordishRules.result(result.puzzle, solved).status)
        }
        assertTrue(g.generate(WordishRequest(0, recentSignatures = recent)) is WordishGenerationResult.Exhausted)
    }

    @Test fun generatedDuplicateLettersStrongestFeedbackAndBudgetMatchLegacy() {
        val p = success(WordishGenerator(source("APPLE")).generate(WordishRequest(9))).puzzle
        assertEquals(listOf(LetterFeedback.CORRECT, LetterFeedback.ELSEWHERE, LetterFeedback.ABSENT,
            LetterFeedback.ELSEWHERE, LetterFeedback.ABSENT), WordishRules.feedback("APPLE", "ALLEY"))
        assertEquals(LetterFeedback.CORRECT, WordishRules.keyboardFeedback(p, mapOf("guesses" to "AMPLE,ALLEY,CRANE"))['P'])
        val wrong = "CRANE,PLANT,BRICK,SHORE,LIGHT,MUSIC"
        assertEquals(PlayStatus.FAILED, WordishRules.result(p, mapOf("guesses" to wrong)).status)
        assertEquals(PlayStatus.COMPLETED, WordishRules.result(p, mapOf("guesses" to "CRANE,PLANT,BRICK,SHORE,LIGHT,APPLE")).status)
        val invalid = WordishRules.submit(p, mapOf("draft" to "ZZZZZ"))
        assertTrue(WordishRules.guesses(invalid).isEmpty())
    }

    @Test fun sourceCopiesAndOldSnapshotsRemainIndependentOfBankChanges() {
        val mutable = mutableListOf("APPLE")
        val bank = WordishAnswerSource(mutable, "test-v1", "Original test")
        val g = WordishGenerator(bank)
        val saved = success(g.generate(WordishRequest(0))).puzzle
        mutable[0] = "CRANE"
        WordishGenerator(source("CRANE")).generate(WordishRequest(0))
        assertEquals("APPLE", saved.data["answer"])
        assertEquals(saved, success(g.generate(WordishRequest(0))).puzzle)
        val old = WordishGame.puzzles.map { it.copy(data = it.data.toMap()) }
        repeat(100) { g.generate(WordishRequest(it.toLong())) }
        assertEquals(old, WordishGame.puzzles)
        assertEquals(listOf("APPLE", "CRANE", "PLANT", "BRICK", "SHORE", "LIGHT", "MUSIC", "DREAM", "FLUTE", "GHOST"), old.map { WordishRules.answer(it) })
        old.forEach { assertEquals(emptyList<String>(), WordishRules.validate(it)) }
        assertEquals("enable-five-v1", saved.data["dictionary"])
        assertTrue(WordishRules.validate(saved.copy(data = saved.data + ("dictionary" to "unknown"))).isNotEmpty())
    }

    @Test fun fullSnapshotCodecSurvivesDifferentBankAndRetainsUnsubmittedDraft() {
        val saved = success(WordishGenerator(source("APPLE")).generate(WordishRequest(0))).puzzle
        val state = mapOf("guesses" to "CRANE", "draft" to "AP")
        val time = "2026-10-25T12:00:00Z"
        val library = Library(listOf(Attempt("generated-save", "wordish", PuzzleMode.EXTRA, null,
            saved, state, time, time, result = WordishRules.result(saved, state))))
        val bytes = LibraryCodec.encode(library)
        WordishGenerator(source("CRANE")).generate(WordishRequest(0))
        val restored = LibraryCodec.decode(bytes)
        assertEquals(library, restored)
        assertEquals("AP", WordishRules.draft(restored.attempts.single().state))
        assertEquals(emptyList<String>(), WordishRules.validate(restored.attempts.single().puzzle))
    }

    @Test fun benchmarkThousandSeedsOnRealBundledBank() {
        val g = WordishGenerator()
        repeat(100) { g.generate(WordishRequest(it.toLong())) }
        val times = mutableListOf<Long>()
        val signatures = mutableSetOf<String>()
        val letterPatterns = mutableSetOf<String>()
        var worstInspected = 0
        repeat(1000) { seed ->
            val start = System.nanoTime()
            val result = success(g.generate(WordishRequest(seed.toLong())))
            times += System.nanoTime() - start
            signatures += result.signature
            val answer = result.puzzle.data.getValue("answer")
            letterPatterns += answer.map { answer.indexOf(it) }.joinToString("")
            worstInspected = maxOf(worstInspected, result.diagnostics.inspected)
            assertEquals(emptyList<String>(), WordishRules.validate(result.puzzle))
        }
        times.sort()
        println("WORDISH_BENCHMARK trials=1000 distinct=${signatures.size} repeatedLetterPatterns=${letterPatterns.size} failures=0 runtimeRejections=0 worstInspected=$worstInspected p50us=${times[499]/1000} p95us=${times[949]/1000} maxus=${times.last()/1000}")
        assertTrue(signatures.size > 300)
    }
}
