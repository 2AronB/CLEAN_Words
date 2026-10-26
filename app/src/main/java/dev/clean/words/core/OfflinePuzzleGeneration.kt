package dev.clean.words.core

import dev.clean.words.GameRegistry
import dev.clean.words.generators.wordish.*
import dev.clean.words.generators.bee.*

/** Small explicit adapters. Loaded/cached on the generation lane, never the input or save lane. */
class OfflinePuzzleGeneration(private val readAsset: (String) -> String) : PuzzleGenerator {
    private var wordish: WordishGenerator? = null
    private var bee: BeeGenerator? = null

    companion object {
        fun isGenerated(gameId: String) = gameId == "wordish" || gameId == "bee"
        const val beePolicy = "${BeeBundledSource.generatorVersion}|${BeeBundledSource.selectionPolicy}|${BeeBundledSource.indexVersion}|${BeeBundledSource.indexSha256}|${BeeBundledSource.dictionaryPolicy}|${BeeBundledSource.familiarityVersion}|${BeeBundledSource.defaultProfileId}|bee-wordcount-v2"
    }

    fun policy(gameId: String): String = when (gameId) {
        "wordish" -> WordishGenerator.runtimePolicy
        "bee" -> beePolicy
        else -> "explicit-starter-cycle-v1"
    }

    fun signature(gameId: String, puzzle: PuzzleRecord): String? = when (gameId) {
        "wordish" -> puzzle.data["answer"]?.takeIf { it.matches(Regex("[A-Z]{5}")) }?.let(WordishGenerator::answerSignature)
        "bee" -> runCatching { BeeGenerator.signature(puzzle.data["letters"].orEmpty(), puzzle.data["center"].orEmpty().single()) }.getOrNull()
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
                when (val result = generator.generate(BeeRequest(request.seed, recentSignatures = request.recentSignatures), cancelled)) {
                    is BeeGenerationResult.Success -> GenerationOutcome.Ready(result.puzzle)
                    is BeeGenerationResult.Cancelled -> GenerationOutcome.Cancelled
                    is BeeGenerationResult.Exhausted -> GenerationOutcome.Unavailable("No fresh Speling Bee letter set and center qualify. The bank is finite. Saved play and earlier puzzles remain available.")
                    is BeeGenerationResult.SearchLimit -> GenerationOutcome.Unavailable("No Speling Bee candidate qualified within the bounded search. Saved puzzles remain available; additional play uses an independent seed.")
                    is BeeGenerationResult.InvalidInput -> GenerationOutcome.Unavailable("Speling Bee source error: ${result.reason}")
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
