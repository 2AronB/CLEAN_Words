package dev.clean.words.generators.wordish

import dev.clean.words.core.PuzzleRecord
import dev.clean.words.games.wordish.WordishDictionary
import dev.clean.words.games.wordish.WordishRules
import java.security.MessageDigest
import java.util.Collections
import java.util.Random

enum class WordishProfile(val policyId: String) {
    STANDARD_SINGLE_FIVE("single-five-six-guesses-v1")
}

data class WordishRequest(
    val seed: Long,
    val profile: WordishProfile = WordishProfile.STANDARD_SINGLE_FIVE,
    val recentSignatures: Set<String> = emptySet(),
    val dictionaryPolicy: String = "enable-five-v1",
)

data class WordishDiagnostics(
    val bankSize: Int,
    val inspected: Int,
    val excluded: Int,
    val candidates: Int,
)

sealed interface WordishGenerationResult {
    data class Success(val puzzle: PuzzleRecord, val signature: String, val diagnostics: WordishDiagnostics) : WordishGenerationResult
    data class Exhausted(val diagnostics: WordishDiagnostics) : WordishGenerationResult
    data class Cancelled(val inspected: Int) : WordishGenerationResult
    data class InvalidInput(val reason: String) : WordishGenerationResult
}

/** A copied, bounded original answer shortlist. Guess eligibility remains a separate pinned policy. */
class WordishAnswerSource(
    words: List<String>,
    val version: String,
    val provenance: String,
) {
    val words: List<String>
    val contentSha256: String
    init {
        require(words.size <= 20_000) { "Answer bank exceeds the bounded 20,000-entry format" }
        this.words = Collections.unmodifiableList(words.sorted())
        contentSha256 = wordishSha256(this.words.joinToString("\n"))
    }
}

class WordishGenerator(private val source: WordishAnswerSource = bundledSource()) {
    companion object {
        const val version = "wordish-selector-v1"
        const val selectionPolicy = "sorted-reservoir-java-random-v1"
        const val sourceVersion = BundledWordishAnswers.version
        const val sourceSha256 = BundledWordishAnswers.sourceSha256
        const val runtimePolicy = "$version|$selectionPolicy|$sourceVersion|$sourceSha256|enable-five-v1|single-five-six-guesses-v1"
        fun answerSignature(answer: String): String {
            require(answer.matches(Regex("[A-Z]{5}")))
            return "wordish:answer:$answer"
        }
        fun bundledSource() = WordishAnswerSource(
            BundledWordishAnswers.words,
            BundledWordishAnswers.version,
            "Original CLEAN Words AI-editorial shortlist; human review pending; TSV SHA-256 ${BundledWordishAnswers.sourceSha256}",
        )
    }

    /** Pure CPU work; caller runs on a worker and freezes Success.puzzle in the existing save. */
    fun generate(request: WordishRequest, isCancelled: () -> Boolean = { false }): WordishGenerationResult {
        if (isCancelled()) return WordishGenerationResult.Cancelled(0)
        if (request.dictionaryPolicy != WordishDictionary.version)
            return WordishGenerationResult.InvalidInput("Unknown guess dictionary policy: ${request.dictionaryPolicy}")
        if (source.version.isBlank() || source.provenance.isBlank())
            return WordishGenerationResult.InvalidInput("Answer bank needs version and provenance")
        val random = Random(request.seed)
        var selected: String? = null
        var inspected = 0
        var excluded = 0
        var candidates = 0
        var previous: String? = null
        for (word in source.words) {
            if (isCancelled()) return WordishGenerationResult.Cancelled(inspected)
            inspected++
            if (!word.matches(Regex("[A-Z]{5}")) || !WordishRules.isAccepted(word) || word == previous)
                return WordishGenerationResult.InvalidInput("Malformed, duplicate or unaccepted answer at sorted entry $inspected: $word")
            previous = word
            if (answerSignature(word) in request.recentSignatures) { excluded++; continue }
            candidates++
            if (random.nextInt(candidates) == 0) selected = word
        }
        val diagnostics = WordishDiagnostics(source.words.size, inspected, excluded, candidates)
        val answer = selected ?: return WordishGenerationResult.Exhausted(diagnostics)
        if (isCancelled()) return WordishGenerationResult.Cancelled(inspected)
        val signature = answerSignature(answer)
        val identity = wordishSha256(listOf(version, selectionPolicy, source.version, source.contentSha256,
            request.profile.policyId, request.dictionaryPolicy, request.seed.toString(), answer).joinToString("|"))
        val data = linkedMapOf(
            "answer" to answer,
            "dictionary" to request.dictionaryPolicy,
            "origin" to source.provenance,
            "generator" to version,
            "selectionPolicy" to selectionPolicy,
            "answerBank" to source.version,
            "answerBankSha256" to source.contentSha256,
            "profile" to request.profile.policyId,
            "seed" to request.seed.toString(),
            "answerSignature" to signature,
            "review" to "ai-editorial-v1; human-review-pending",
            "difficulty" to "provisional-uncalibrated",
        )
        val puzzle = PuzzleRecord("wordish-generated-$identity", 1, "tWordle", Collections.unmodifiableMap(data))
        check(WordishRules.validate(puzzle).isEmpty())
        return WordishGenerationResult.Success(puzzle, signature, diagnostics)
    }
}

internal fun wordishSha256(text: String): String = MessageDigest.getInstance("SHA-256")
    .digest(text.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
