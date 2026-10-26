package dev.clean.words.core

import dev.clean.words.GameRegistry
import dev.clean.words.generators.wordish.*
import dev.clean.words.generators.bee.*
import dev.clean.words.generators.connections.*
import dev.clean.words.generators.crossword.*
import dev.clean.words.games.bee.BeeOpening
import dev.clean.words.games.bee.BeeOpeningQuality

/** Small explicit adapters. Loaded/cached on the generation lane, never the input or save lane. */
class OfflinePuzzleGeneration(private val readAsset: (String) -> String) : PuzzleGenerator {
    private var wordish: WordishGenerator? = null
    private var bee: BeeGenerator? = null
    private var connections: ConnectionsGenerator? = null
    private var crossword: CrosswordGenerator? = null

    companion object {
        fun isGenerated(gameId: String) = gameId == "wordish" || gameId == "bee" || gameId == "connections" || gameId == "crossword"
        val crosswordPolicy = "${CrosswordGenerator.VERSION}|${CrosswordGenerator.POLICY}|${BundledCrosswordBank.version}|${BundledCrosswordBank.contentSha256}|${BundledCrosswordBank.sourceSha256}|7x7|250000-steps|32-candidates"
        const val connectionsPolicy = "${ConnectionsGenerator.VERSION}|${ConnectionsGenerator.POLICY}|${BundledConnectionsBank.version}|${BundledConnectionsBank.contentSha256}|${BundledConnectionsBank.sourceSha256}|two-decoys|250000-steps|500-candidates"
        const val beeOpeningApproval = "owner-no-ratio-filter-2026-10-26-v1"
        const val beePolicy = "${BeeGenerator.openingVersion}|${BeeGenerator.openingProfileVersion}|${BeeBundledSource.selectionPolicy}|${BeeBundledSource.indexVersion}|${BeeBundledSource.indexSha256}|${BeeBundledSource.dictionaryPolicy}|${BeeBundledSource.familiarityVersion}|${BeeBundledSource.defaultProfileId}|${BeeOpening.SCORING}|${BeeOpening.BENCHMARK_POLICY}|${BeeOpening.ROUNDING}|ratio-permille=0|$beeOpeningApproval"
    }

    fun policy(gameId: String): String = when (gameId) {
        "wordish" -> WordishGenerator.runtimePolicy
        "bee" -> beePolicy
        "connections" -> connectionsPolicy
        "crossword" -> crosswordPolicy
        else -> "explicit-starter-cycle-v1"
    }

    fun signature(gameId: String, puzzle: PuzzleRecord): String? = when (gameId) {
        "wordish" -> puzzle.data["answer"]?.takeIf { it.matches(Regex("[A-Z]{5}")) }?.let(WordishGenerator::answerSignature)
        "bee" -> runCatching { BeeGenerator.signature(puzzle.data["letters"].orEmpty(), puzzle.data["center"].orEmpty().single()) }.getOrNull()
        "connections" -> runCatching { ConnectionsGenerator.semanticSignature(puzzle) }.getOrNull()
        "crossword" -> runCatching { CrosswordGenerator.semanticSignature(puzzle) }.getOrNull()
        else -> null
    }

    override fun generate(request: GenerationRequest, cancelled: () -> Boolean): GenerationOutcome {
        if (cancelled()) return GenerationOutcome.Cancelled
        if (request.policy != policy(request.gameId)) return GenerationOutcome.Unavailable("This generation policy is unsupported. Saved puzzles are preserved.")
        return when (request.gameId) {
            "wordish" -> {
                val generator = wordish ?: WordishGenerator().also { wordish = it }
                when (val result = generator.generate(WordishRequest(request.seed, recentSignatures = request.recentSignatures), cancelled)) {
                    is WordishGenerationResult.Success -> GenerationOutcome.Ready(result.puzzle)
                    is WordishGenerationResult.Cancelled -> GenerationOutcome.Cancelled
                    is WordishGenerationResult.Exhausted -> GenerationOutcome.Unavailable("No fresh tWordle answer qualifies outside the recent window. The answer bank is finite. Saved play and earlier puzzles remain available.")
                    is WordishGenerationResult.InvalidInput -> GenerationOutcome.Unavailable("tWordle source error: ${result.reason}")
                }
            }
            "bee" -> {
                val generator = bee ?: try {
                    BeeGenerator(BeeVocabulary.fromBundledIndex(readAsset(BeeBundledSource.indexPath), cancelled)).also { bee = it }
                } catch (_: BeePreparationCancelled) { return GenerationOutcome.Cancelled }
                when (val result = generator.generateOpening(BeeRequest(request.seed, recentSignatures = request.recentSignatures),
                    BeeOpeningQuality(0, beeOpeningApproval), cancelled)) {
                    is BeeGenerationResult.Success -> GenerationOutcome.Ready(result.puzzle)
                    is BeeGenerationResult.Cancelled -> GenerationOutcome.Cancelled
                    is BeeGenerationResult.Exhausted -> GenerationOutcome.Unavailable("No fresh Speling Bee letter set and center qualify. The bank is finite. Saved play and earlier puzzles remain available.")
                    is BeeGenerationResult.SearchLimit -> GenerationOutcome.Unavailable("No Speling Bee candidate qualified within the bounded search. Saved puzzles remain available; additional play uses an independent seed.")
                    is BeeGenerationResult.InvalidInput -> GenerationOutcome.Unavailable("Speling Bee source error: ${result.reason}")
                }
            }
            "connections" -> {
                val generator = connections ?: ConnectionsGenerator(BundledConnectionsBank.bank).also { connections = it }
                when (val result = generator.generate(request.seed, ConnectionsProfile(BundledConnectionsBank.contentSha256), request.recentSignatures, cancelled)) {
                    is ConnectionsGenerationResult.Success -> GenerationOutcome.Ready(result.puzzle)
                    is ConnectionsGenerationResult.Cancelled -> GenerationOutcome.Cancelled
                    is ConnectionsGenerationResult.NoCandidate -> GenerationOutcome.Unavailable(when (result.reason) {
                        ConnectionsFailure.BUDGET_EXHAUSTED -> "No Connektions board qualified within the bounded search. Saved puzzles remain available; retry or try additional play with another seed."
                        ConnectionsFailure.RECENT_POOL_EXHAUSTED -> "No fresh Connektions board qualifies outside your recent window. The relationship bank is finite. Saved puzzles remain available."
                        ConnectionsFailure.IMPOSSIBLE_CONSTRAINTS -> "The Connektions bank cannot compose a board meeting its decoy and solution rules. Saved puzzles remain available."
                        else -> "Connektions source or policy validation failed. Saved puzzles are preserved; please report this packaging error."
                    })
                }
            }
            "crossword" -> {
                val generator = crossword ?: CrosswordGenerator(BundledCrosswordBank.bank).also { crossword = it }
                when (val result = generator.generate(request.seed, CrosswordProfile(), request.recentSignatures, cancelled)) {
                    is CrosswordGenerationResult.Success -> GenerationOutcome.Ready(result.puzzle)
                    is CrosswordGenerationResult.Cancelled -> GenerationOutcome.Cancelled
                    is CrosswordGenerationResult.NoCandidate -> GenerationOutcome.Unavailable(when (result.reason) {
                        CrosswordFailure.BUDGET_EXHAUSTED -> "No Crossita fill qualified within the bounded search. Saved puzzles remain available; retry or try another additional seed."
                        CrosswordFailure.RECENT_POOL_EXHAUSTED -> "No fresh Crossita fill qualifies outside your recent window. The clue pilot is finite. Saved puzzles remain available."
                        CrosswordFailure.IMPOSSIBLE_CONSTRAINTS -> "The Crossita bank cannot fill a qualifying layout. Saved puzzles remain available."
                        else -> "Crossita source or policy validation failed. Saved puzzles are preserved; please report this packaging error."
                    })
                }
            }
            else -> {
                // Explicitly disclosed unchanged starter flow for modules without a delivered generator.
                val game = GameRegistry.get(request.gameId)
                val index = if (request.mode == PuzzleMode.DAILY) AttemptRules.dailyIndex(game.id, request.date, game.puzzles.size)
                    else Math.floorMod(request.starterOrdinal, game.puzzles.size)
                GenerationOutcome.Ready(game.puzzles[index])
            }
        }
    }
}
