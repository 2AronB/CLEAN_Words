package dev.clean.words.core

import dev.clean.words.GameRegistry
import dev.clean.words.games.bee.BeePuzzle
import dev.clean.words.games.bee.BeeRules
import dev.clean.words.games.bee.BeeState
import dev.clean.words.games.wordish.WordishGame
import dev.clean.words.generators.bee.BeeBundledSource
import dev.clean.words.generators.bee.BeeGenerator
import dev.clean.words.generators.bee.BeeVocabulary
import dev.clean.words.generators.bee.BeeRequest
import dev.clean.words.generators.bee.BeeGenerationResult
import dev.clean.words.games.bee.BeeOpening
import dev.clean.words.games.bee.BeeOpeningQuality
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class OfflinePuzzleGenerationTest {
    private val day = LocalDate.of(2026,10,25)
    private fun readAsset(name: String): String {
        val file = listOf(File("src/main/assets/$name"), File("app/src/main/assets/$name")).first { it.exists() }
        return file.readText(Charsets.UTF_8)
    }
    private fun request(engine: OfflinePuzzleGeneration, game: String, mode: PuzzleMode = PuzzleMode.DAILY, id: String = "id", recent: Set<String> = emptySet()): GenerationRequest {
        val policy = engine.policy(game)
        return GenerationRequest(game, mode, day,
            if (mode == PuzzleMode.DAILY) GenerationSeeds.daily(game, policy, day) else GenerationSeeds.extra(game, policy, id),
            id, policy, recent)
    }

    @Test fun activeOpeningThousandSeedsRetainBaseVarietyAndApprovedZeroFilter() {
        val generator = BeeGenerator(BeeVocabulary.fromBundledIndex(readAsset(BeeBundledSource.indexPath)))
        val signatures = mutableSetOf<String>(); val sets = mutableSetOf<String>()
        var inspected = 0; var minGoal = Int.MAX_VALUE; var maxGoal = 0
        repeat(1000) { seed ->
            val result = generator.generateOpening(BeeRequest(seed.toLong()),
                BeeOpeningQuality(0, OfflinePuzzleGeneration.beeOpeningApproval)) as BeeGenerationResult.Success
            val board = BeePuzzle.from(result.puzzle)
            assertEquals(emptyList<String>(), BeeRules.validate(board))
            assertEquals("0", result.puzzle.data["minimumFamiliarRatioPermille"])
            assertEquals(0, BeeOpening.progress(board, BeeState()).words.size)
            signatures += result.signature; sets += board.letters
            inspected = maxOf(inspected, result.diagnostics.inspected)
            minGoal = minOf(minGoal, board.wordTarget); maxGoal = maxOf(maxGoal, board.wordTarget)
        }
        println("BEE_ACTIVE_OPENING trials=1000 distinctSetCenters=${signatures.size} distinctSets=${sets.size} failures=0 worstInspected=$inspected goalRange=$minGoal..$maxGoal ratioPermille=0")
        assertEquals(719, signatures.size); assertEquals(266, sets.size)
    }

    @Test fun realAdaptersPinDailyIndependentOfExtrasAndExcludeMeaningfulContent() {
        val e = OfflinePuzzleGeneration(::readAsset)
        listOf("wordish", "bee").forEach { game ->
            val req = request(e, game)
            val daily = (e.generate(req) { false } as GenerationOutcome.Ready).puzzle
            val signatures = mutableSetOf(e.signature(game, daily)!!)
            repeat(12) { index ->
                val extra = e.generate(request(e, game, PuzzleMode.EXTRA, "extra-$index", signatures)) { false } as GenerationOutcome.Ready
                assertTrue(signatures.add(e.signature(game, extra.puzzle)!!))
                assertEquals(emptyList<String>(), GameRegistry.get(game).validate(extra.puzzle))
            }
            assertEquals(daily, (e.generate(req) { false } as GenerationOutcome.Ready).puzzle)
            assertEquals(daily, (OfflinePuzzleGeneration(::readAsset).generate(req) { false } as GenerationOutcome.Ready).puzzle)
        }
    }

    @Test fun frozenSnapshotsSurvivePolicyChangesAndLegacyDailyAlwaysWins() {
        val e = OfflinePuzzleGeneration(::readAsset)
        val now = Instant.parse("2026-10-25T12:00:00Z"); val zone = ZoneId.of("UTC")
        val old = AttemptRules.create(Library(), WordishGame, PuzzleMode.DAILY, now, zone)
        val library = Library(listOf(old))
        val new = (e.generate(request(e, "wordish")) { false } as GenerationOutcome.Ready).puzzle
        assertEquals(old, AttemptRules.createSnapshot(library, WordishGame, PuzzleMode.DAILY, new, now, zone))
        val bee = (e.generate(request(e, "bee")) { false } as GenerationOutcome.Ready).puzzle
        val board = BeePuzzle.from(bee)
        var state = BeeState()
        board.goalWords.take(board.wordTarget).forEach { state = BeeRules.submit(board, state, it) }
        assertEquals(PlayStatus.COMPLETED, BeeRules.result(board, state).status)
        val a = AttemptRules.createSnapshot(library, GameRegistry.get("bee"), PuzzleMode.EXTRA, bee, now, zone).let {
            AttemptRules.update(it, state.toMap(), GameRegistry.get("bee"), now)
        }
        val roundTrip = LibraryCodec.decode(LibraryCodec.encode(Library(listOf(old, a))))
        assertEquals(bee, roundTrip.attempts.last().puzzle)
        assertEquals(BeeRules.result(board, state), GameRegistry.get("bee").result(roundTrip.attempts.last().puzzle, state.toMap()))
        assertTrue(e.generate(request(e, "bee").copy(policy = "future-source")) { false } is GenerationOutcome.Unavailable)
        assertEquals(old, roundTrip.attempts.first())
    }

    @Test fun sourceFailuresAreVisibleAndRetryDoesNotCacheFailure() {
        var broken = true; var reads = 0
        val e = OfflinePuzzleGeneration { reads++; if (broken) "bad index" else readAsset(it) }
        assertThrows(IllegalArgumentException::class.java) { e.generate(request(e, "bee")) { false } }
        broken = false
        assertTrue(e.generate(request(e, "bee")) { false } is GenerationOutcome.Ready)
        e.generate(request(e, "bee")) { false }
        assertEquals(2, reads)
        assertEquals(GenerationOutcome.Cancelled, e.generate(request(e, "bee")) { true })
    }

    @Test fun canonicalBeeSignatureIgnoresLetterOrderAndStarterFlowStaysHonest() {
        val e = OfflinePuzzleGeneration(::readAsset)
        assertEquals(BeeGenerator.signature("CENTRAL", 'A'), BeeGenerator.signature("LARTNEC", 'A'))
        assertNotEquals(BeeGenerator.signature("CENTRAL", 'A'), BeeGenerator.signature("LARTNEC", 'E'))
        assertTrue(e.policy("bee").contains(BeeBundledSource.indexSha256))
        listOf("connections", "strands", "crossword").forEach { id ->
            assertFalse(OfflinePuzzleGeneration.isGenerated(id))
            val game = GameRegistry.get(id)
            repeat(12) { ordinal ->
                val p = (e.generate(request(e, id, PuzzleMode.EXTRA).copy(starterOrdinal = ordinal)) { false } as GenerationOutcome.Ready).puzzle
                assertEquals(game.puzzles[ordinal % 10], p); assertFalse(p.data.containsKey("generator"))
            }
        }
    }
}
