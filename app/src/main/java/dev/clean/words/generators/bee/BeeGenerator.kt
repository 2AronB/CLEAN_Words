package dev.clean.words.generators.bee

import dev.clean.words.core.PuzzleRecord
import dev.clean.words.games.bee.BeePuzzle
import dev.clean.words.games.bee.BeeRules
import dev.clean.words.games.bee.BeeOpening
import dev.clean.words.games.bee.BeeOpeningQuality
import java.security.MessageDigest
import java.util.Collections
import java.util.Random

private val beeWordPattern = Regex("[A-Z]{4,32}")

/** Engineering quality filters, not calibrated difficulty labels. */
data class BeeProfile(
    val id: String = BeeBundledSource.defaultProfileId,
    val minFamiliarWords: Int = 12,
    val minAcceptedWords: Int = 20,
    val maxAcceptedWords: Int = 400,
    val minFamiliarLengthBuckets: Int = 3,
    val requireFamiliarFour: Boolean = true,
    val maxCandidateAttempts: Int = 256,
)

data class BeeRequest(
    val seed: Long,
    val profile: BeeProfile = BeeProfile(),
    val recentSignatures: Set<String> = emptySet(),
)

data class BeeDiagnostics(
    val indexedWords: Int,
    val candidateSets: Int,
    val candidateSetCenters: Int,
    val inspected: Int,
    val excluded: Int,
    val evaluated: Int,
    val rejections: Map<String, Int>,
)

sealed interface BeeGenerationResult {
    data class Success(val puzzle: PuzzleRecord, val signature: String, val diagnostics: BeeDiagnostics) : BeeGenerationResult
    data class Exhausted(val diagnostics: BeeDiagnostics) : BeeGenerationResult
    data class SearchLimit(val diagnostics: BeeDiagnostics) : BeeGenerationResult
    data class Cancelled(val inspected: Int) : BeeGenerationResult
    data class InvalidInput(val reason: String) : BeeGenerationResult
}

class BeePreparationCancelled : RuntimeException("Bee index preparation cancelled")

/** Immutable, copied vocabulary. Grouping indexes words, not finished boards. Prepare on a worker. */
class BeeVocabulary(
    words: List<String>,
    familiarWords: Set<String>,
    val dictionaryVersion: String,
    val familiarityVersion: String,
    val sourceHash: String = beeSha256(words.sorted().joinToString("\n") + "|" + familiarWords.sorted().joinToString("\n")),
    isCancelled: () -> Boolean = { false },
    val familiarityEvidence: String = "original-ai-editorial; human-review-pending; frequency-unknown",
) {
    val words: List<String>
    val familiarWords: Set<String>
    internal val byMask: Map<Int, List<String>>
    internal val candidateMasks: List<Int>

    init {
        require(words.size <= 200_000) { "Bee vocabulary exceeds 200,000-word bound" }
        require(dictionaryVersion.isNotBlank() && familiarityVersion.isNotBlank() && sourceHash.isNotBlank())
        require(familiarityEvidence.isNotBlank())
        val copied = words.sorted()
        val known = familiarWords.toSet()
        val grouped = sortedMapOf<Int, MutableList<String>>()
        var previous: String? = null
        for (word in copied) {
            if (isCancelled()) throw BeePreparationCancelled()
            require(word.matches(beeWordPattern) && word != previous) { "Malformed or duplicate Bee word: $word" }
            val mask = beeMask(word)
            require(Integer.bitCount(mask) <= 7) { "Index contains an impossible seven-letter word: $word" }
            grouped.getOrPut(mask) { mutableListOf() }.add(word)
            previous = word
        }
        require(copied.toSet().containsAll(known)) { "Familiar words must be accepted vocabulary entries" }
        this.words = Collections.unmodifiableList(copied)
        this.familiarWords = Collections.unmodifiableSet(known)
        byMask = Collections.unmodifiableMap(grouped.mapValues { Collections.unmodifiableList(it.value) })
        candidateMasks = Collections.unmodifiableList(known.map(::beeMask).filter { Integer.bitCount(it) == 7 }.distinct().sorted())
    }

    companion object {
        /** Verify the packaged input. Independent source filtering is covered by corpus tests. */
        fun fromBundledIndex(text: String, isCancelled: () -> Boolean = { false }): BeeVocabulary {
            if (isCancelled()) throw BeePreparationCancelled()
            require(text.length <= 8 * 1024 * 1024) { "Bee index exceeds bounded format" }
            require(beeSha256(text) == BeeBundledSource.indexSha256) { "Bee index checksum mismatch" }
            val words = ArrayList<String>(BeeBundledSource.indexWordCount)
            val familiar = mutableSetOf<String>()
            text.lineSequence().forEachIndexed { index, line ->
                if (isCancelled()) throw BeePreparationCancelled()
                if (index == 0) require(line == "mask\tword\tfamiliar") { "Unknown Bee index header" }
                else if (line.isNotEmpty()) {
                    val cells = line.split('\t')
                    require(cells.size == 3 && cells[0].toIntOrNull() == beeMask(cells[1]) && cells[2] in setOf("0", "1")) { "Invalid Bee index line ${index + 1}" }
                    words.add(cells[1])
                    if (cells[2] == "1") familiar.add(cells[1])
                }
            }
            require(words.size == BeeBundledSource.indexWordCount && familiar.size == BeeBundledSource.familiarCount) { "Bee index counts mismatch" }
            return BeeVocabulary(words, familiar,
                "ENABLE1-sha256:${BeeBundledSource.dictionarySha256}", BeeBundledSource.familiarityVersion,
                BeeBundledSource.indexSha256, isCancelled)
        }
    }
}

class BeeGenerator(private val vocabulary: BeeVocabulary) {
    companion object {
        const val version = BeeBundledSource.generatorVersion
        const val selectionPolicy = BeeBundledSource.selectionPolicy
        const val openingVersion = "bee-seven-opening-selector-v1"
        const val openingProfileVersion = "bee-opening-quality-v1"
        fun signature(letters: String, center: Char): String {
            require(letters.length == 7 && letters.toSet().size == 7 && letters.all { it in 'A'..'Z' } && center in letters)
            return "bee:letters:${letters.toList().sorted().joinToString("")}:center:$center"
        }
    }

    /** No IO. Bounded shuffled candidate selection; every accepted word is frozen on success. */
    fun generate(request: BeeRequest, isCancelled: () -> Boolean = { false }): BeeGenerationResult =
        generateWithPolicy(request, null, isCancelled)

    /** Explicit opt-in only. Production must supply the owner's versioned quality decision. */
    fun generateOpening(request: BeeRequest, quality: BeeOpeningQuality?,
        isCancelled: () -> Boolean = { false }): BeeGenerationResult {
        if (isCancelled()) return BeeGenerationResult.Cancelled(0)
        if (quality == null) return BeeGenerationResult.InvalidInput("Opening quality threshold awaits owner approval")
        return generateWithPolicy(request, quality, isCancelled)
    }

    private fun generateWithPolicy(request: BeeRequest, quality: BeeOpeningQuality?,
        isCancelled: () -> Boolean): BeeGenerationResult {
        if (isCancelled()) return BeeGenerationResult.Cancelled(0)
        val p = request.profile
        if (p.id.isBlank() || p.minFamiliarWords !in 1..10_000 || p.minAcceptedWords !in 1..10_000 ||
            p.maxAcceptedWords !in p.minAcceptedWords..10_000 || p.minFamiliarLengthBuckets !in 1..29 || p.maxCandidateAttempts !in 1..10_000)
            return BeeGenerationResult.InvalidInput("Invalid bounded seven-letter profile")
        val candidates = vocabulary.candidateMasks.flatMap { mask -> beeLetters(mask).map { mask to it } }.toMutableList()
        val random = Random(request.seed)
        for (i in candidates.lastIndex downTo 1) {
            if (isCancelled()) return BeeGenerationResult.Cancelled(0)
            val other = random.nextInt(i + 1)
            val saved = candidates[i]; candidates[i] = candidates[other]; candidates[other] = saved
        }
        var inspected = 0
        var excluded = 0
        var evaluated = 0
        val rejectionCounts = sortedMapOf<String, Int>()
        fun diagnostics() = BeeDiagnostics(vocabulary.words.size, vocabulary.candidateMasks.size, candidates.size,
            inspected, excluded, evaluated, rejectionCounts.toMap())
        for ((mask, center) in candidates) {
            if (isCancelled()) return BeeGenerationResult.Cancelled(inspected)
            val letters = beeLetters(mask)
            val signature = signature(letters, center)
            inspected++
            if (signature in request.recentSignatures) { excluded++; continue }
            if (evaluated >= p.maxCandidateAttempts) return BeeGenerationResult.SearchLimit(diagnostics())
            evaluated++
            val accepted = sortedSetOf<String>()
            var subset = mask
            while (subset > 0) {
                if (isCancelled()) return BeeGenerationResult.Cancelled(inspected)
                if (subset and (1 shl (center - 'A')) != 0) {
                    for (word in vocabulary.byMask[subset].orEmpty()) {
                        if (isCancelled()) return BeeGenerationResult.Cancelled(inspected)
                        accepted.add(word)
                    }
                }
                subset = (subset - 1) and mask
            }
            val familiar = accepted.intersect(vocabulary.familiarWords)
            val pangrams = accepted.filter { beeMask(it) == mask }
            val board = BeePuzzle(letters, center, familiar, accepted, vocabulary.dictionaryVersion, BeePuzzle.WORD_COUNT_SCORING)
            val failed = buildList {
                if (familiar.size < p.minFamiliarWords) add("too-few-familiar")
                if (accepted.size !in p.minAcceptedWords..p.maxAcceptedWords) add("accepted-count")
                if (familiar.map { it.length }.distinct().size < p.minFamiliarLengthBuckets) add("familiar-length-mix")
                if (p.requireFamiliarFour && familiar.none { it.length == 4 }) add("no-familiar-four")
                if (pangrams.none { it in familiar }) add("no-familiar-pangram")
                if (quality != null) {
                    if (accepted.size < BeeOpening.LENGTH || familiar.size < BeeOpening.LENGTH) add("opening-too-few-words")
                    if (BeeOpening.benchmark(board, familiar).toLong() * 1000 <
                        BeeOpening.benchmark(board).toLong() * quality.minimumRatioPermille) add("opening-familiar-ratio")
                }
            }
            if (failed.isNotEmpty()) {
                failed.forEach { rejectionCounts[it] = (rejectionCounts[it] ?: 0) + 1 }
                continue
            }
            val profileSnapshot = listOf(p.id, p.minFamiliarWords, p.minAcceptedWords, p.maxAcceptedWords,
                p.minFamiliarLengthBuckets, p.requireFamiliarFour, p.maxCandidateAttempts).joinToString("|")
            val selectedVersion = if (quality == null) version else openingVersion
            val selectedProfile = if (quality == null) p.id else "${p.id}:$openingProfileVersion"
            val selectedThresholds = if (quality == null) profileSnapshot else listOf(profileSnapshot, openingProfileVersion,
                BeeOpening.SCORING, BeeOpening.BENCHMARK_POLICY, quality.minimumRatioPermille, quality.approvalVersion,
                vocabulary.familiarityEvidence).joinToString("|")
            val identity = beeSha256(listOf(selectedVersion, selectionPolicy, vocabulary.sourceHash, vocabulary.dictionaryVersion,
                vocabulary.familiarityVersion, selectedThresholds, request.seed, signature).joinToString("|"))
            val snapshot = board.snapshot("bee-generated-$identity")
            val data = snapshot.data + mapOf(
                "generator" to selectedVersion, "selectionPolicy" to selectionPolicy,
                "profile" to selectedProfile, "profileThresholds" to selectedThresholds, "seed" to request.seed.toString(),
                "contentSignature" to signature, "sourceIndex" to BeeBundledSource.indexVersion,
                "sourceIndexSha256" to vocabulary.sourceHash, "dictionaryPolicy" to BeeBundledSource.dictionaryPolicy,
                "familiarityVersion" to vocabulary.familiarityVersion,
                "review" to if (quality == null) "original-ai-editorial; human-review-pending; frequency-unknown" else vocabulary.familiarityEvidence,
                "difficulty" to "provisional-uncalibrated", "wordTarget" to board.wordTarget.toString(),
                "pangrams" to pangrams.joinToString(","),
                "lengthTotals" to accepted.groupingBy { it.length }.eachCount().toSortedMap().entries.joinToString(",") { "${it.key}:${it.value}" },
                "origin" to "Public-domain ENABLE by Alan Beale with M. Cooper/contributors; original CLEAN Words AI editorial familiarity shortlist; see bundled notice and generation/bee/policy.json",
            )
            check(BeeRules.validate(board).isEmpty())
            val frozen = if (quality == null) snapshot.copy(data = Collections.unmodifiableMap(data))
                else BeeOpening.freeze(board, data.filterKeys { it !in snapshot.data || it == "wordTarget" }, quality)
                    .snapshot(snapshot.id).let { it.copy(data = Collections.unmodifiableMap(it.data)) }
            if (isCancelled()) return BeeGenerationResult.Cancelled(inspected)
            return BeeGenerationResult.Success(frozen, signature, diagnostics())
        }
        return BeeGenerationResult.Exhausted(diagnostics())
    }
}

internal fun beeMask(word: String): Int = word.fold(0) { result, c -> result or (1 shl (c - 'A')) }
internal fun beeLetters(mask: Int) = ('A'..'Z').filter { mask and (1 shl (it - 'A')) != 0 }.joinToString("")
internal fun beeSha256(text: String): String = MessageDigest.getInstance("SHA-256")
    .digest(text.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
