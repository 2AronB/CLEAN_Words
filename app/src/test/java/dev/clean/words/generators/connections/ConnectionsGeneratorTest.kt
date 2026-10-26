package dev.clean.words.generators.connections

import dev.clean.words.core.Attempt
import dev.clean.words.core.Library
import dev.clean.words.core.LibraryCodec
import dev.clean.words.core.PlayStatus
import dev.clean.words.core.PuzzleMode
import dev.clean.words.core.PuzzleRecord
import dev.clean.words.games.connections.ConnectionsContent
import dev.clean.words.games.connections.ConnectionsRules
import org.junit.Assert.*
import org.junit.Test

/** Checks outputs independently of the composer, including the full recorded relationship model. */
class ConnectionsGeneratorTest {
    private val pilot get() = ConnectionsPilot.bank
    private fun profile(bank: ConnectionsBank) = ConnectionsProfile(bank.contentSha256)
    private fun success(result: ConnectionsGenerationResult): ConnectionsGenerationResult.Success {
        assertTrue("Expected generated board, received $result", result is ConnectionsGenerationResult.Success)
        return result as ConnectionsGenerationResult.Success
    }
    private fun relation(id: String, words: String) = ConnectionsRelation(
        id, "Original $id fixture", "Test membership in $id", words.split(' ').map { ConnectionsMember(it, "$it fixture sense") },
    )
    private fun oneBoardBank() = ConnectionsBank(
        "single-board-test-v1", "Original test inputs, not a published puzzle",
        listOf(
            relation("body", "FOOT HAND ARM LEG"),
            relation("containers", "BASKET CUP BIN CRATE"),
            relation("cookware", "PAN WOK SKILLET SAUCEPAN"),
            relation("cleaning", "SPONGE MOP BROOM DUSTER"),
        ),
        listOf(relation("ball", "FOOT HAND BASKET BASE"), relation("cake", "CUP PAN SPONGE CHEESE")),
    )

    @Test fun sameSeedAndCanonicalInputOrderProduceSameFrozenBoard() {
        val source = pilot
        val reversed = source.copy(
            categories = source.categories.reversed().map { it.copy(members = it.members.reversed()) },
            decoyRelations = source.decoyRelations.reversed().map { it.copy(members = it.members.reversed()) },
        )
        assertEquals(source.contentSha256, reversed.contentSha256)
        val generator = ConnectionsGenerator(source)
        val first = success(generator.generate(0, profile(source)))
        assertEquals(first, generator.generate(0, profile(source)))
        assertEquals(first, ConnectionsGenerator(reversed).generate(0, profile(reversed)))
        assertEquals(first.signature, ConnectionsGenerator.semanticSignature(first.puzzle))
    }

    @Test fun actualPilotGeneratesDifferentSemanticBoardsWithoutFixtureCycling() {
        val source = pilot
        val generated = listOf(0L, 1L, 2L, 3L, Long.MIN_VALUE, Long.MAX_VALUE).map {
            success(ConnectionsGenerator(source).generate(it, profile(source)))
        }
        assertTrue("These few seeds should generate more than the two authored fixtures", generated.map { it.signature }.distinct().size > 2)
        generated.forEach { assertIndependentBoard(source, it) }
    }

    @Test fun successfulSnapshotContainsCompletePlayableContentAndAllModeledQuartets() {
        val source = pilot
        val result = success(ConnectionsGenerator(source).generate(0, profile(source)))
        assertIndependentBoard(source, result)
        var state = emptyMap<String, String>()
        for (group in 0..3) {
            for (tile in group * 4 until group * 4 + 4) state = ConnectionsRules.select(state, tile)
            state = ConnectionsRules.submit(result.puzzle, state)
        }
        assertEquals(PlayStatus.COMPLETED, ConnectionsRules.result(state).status)
        assertEquals(0, ConnectionsRules.mistakes(state).size)
        assertTrue(result.puzzle.data.getValue("origin").isNotBlank())
        assertTrue(result.diagnostics.steps <= profile(source).maxSteps)
        assertTrue(result.diagnostics.candidates <= profile(source).maxCandidates)
    }

    @Test fun semanticSignatureIgnoresOrderLabelsIdentityAndGeneratorVersion() {
        val result = success(ConnectionsGenerator(pilot).generate(0, profile(pilot)))
        val original = result.puzzle
        val groups = ConnectionsRules.groups(original).reversed()
        val reordered = original.copy(id = "different-id", revision = 123, title = "Other title", data = original.data + buildMap {
            put("generator.version", "future-version")
            groups.forEachIndexed { i, group ->
                put("group.$i.words", group.words.reversed().joinToString("|"))
                put("group.$i.label", "Other label $i")
            }
        })
        assertEquals(result.signature, ConnectionsGenerator.semanticSignature(reordered))
        ConnectionsContent.puzzles.forEach { assertTrue(ConnectionsGenerator.semanticSignature(it).isNotBlank()) }
    }

    @Test fun exclusionsSkipContentRatherThanOnlyChangingIdentityOrTileOrder() {
        val source = pilot
        val generator = ConnectionsGenerator(source)
        val first = success(generator.generate(0, profile(source)))
        val second = success(generator.generate(0, profile(source), setOf(first.signature)))
        assertNotEquals(first.signature, second.signature)
        assertNotEquals(ConnectionsGenerator.semanticSignature(first.puzzle), ConnectionsGenerator.semanticSignature(second.puzzle))
        assertIndependentBoard(source, second)
    }

    @Test fun trulyExhaustedTinyPoolIsReportedWithoutWeakeningTheDecoyTarget() {
        val source = oneBoardBank()
        assertEquals(emptyList<String>(), ConnectionsGenerator.validateSource(source))
        val generator = ConnectionsGenerator(source)
        val only = success(generator.generate(0, profile(source)))
        assertIndependentBoard(source, only)
        val excluded = generator.generate(1, profile(source), setOf(only.signature))
        assertTrue(excluded.toString(), excluded is ConnectionsGenerationResult.NoCandidate)
        assertEquals(ConnectionsFailure.RECENT_POOL_EXHAUSTED, (excluded as ConnectionsGenerationResult.NoCandidate).reason)
    }

    @Test fun insufficientOverlapAndTooFewCategoriesAreHonestImpossibleConstraints() {
        val full = oneBoardBank()
        val absent = full.copy(decoyRelations = listOf(
            relation("absent-a", "RED BLUE GREEN GOLD"), relation("absent-b", "NORTH SOUTH EAST WEST"),
        ))
        listOf(absent, full.copy(categories = full.categories.take(3))).forEach { source ->
            assertEquals(emptyList<String>(), ConnectionsGenerator.validateSource(source))
            val result = ConnectionsGenerator(source).generate(0, profile(source))
            assertTrue(result.toString(), result is ConnectionsGenerationResult.NoCandidate)
            assertEquals(ConnectionsFailure.IMPOSSIBLE_CONSTRAINTS, (result as ConnectionsGenerationResult.NoCandidate).reason)
        }
    }

    @Test fun smallBudgetsDoNotClaimTheWholePoolIsExhausted() {
        val source = pilot
        val result = ConnectionsGenerator(source).generate(0, profile(source).copy(maxSteps = 1))
        assertTrue(result.toString(), result is ConnectionsGenerationResult.NoCandidate)
        val failure = result as ConnectionsGenerationResult.NoCandidate
        assertEquals(ConnectionsFailure.BUDGET_EXHAUSTED, failure.reason)
        assertTrue(failure.diagnostics.steps <= 1)
    }

    @Test fun competingRecordedPartitionIsRejectedRatherThanPresentedAsFair() {
        val source = oneBoardBank().let { it.copy(decoyRelations = it.decoyRelations + listOf(
            relation("alternative-a", "FOOT HAND BASKET CUP"),
            relation("alternative-b", "ARM LEG BIN CRATE"),
        )) }
        val result = ConnectionsGenerator(source).generate(0, profile(source))
        assertTrue(result.toString(), result is ConnectionsGenerationResult.NoCandidate)
        val failure = result as ConnectionsGenerationResult.NoCandidate
        assertEquals(ConnectionsFailure.IMPOSSIBLE_CONSTRAINTS, failure.reason)
        assertTrue((failure.diagnostics.rejections["competing-partition"] ?: 0) > 0)
    }

    @Test fun fourthVisibleDecoyMemberCannotBeIgnoredToKeepAChosenTriple() {
        val original = oneBoardBank()
        val source = original.copy(decoyRelations = original.decoyRelations.mapIndexed { i, r ->
            if (i == 0) r.copy(members = r.members + ConnectionsMember("ARM", "armball test relationship")) else r
        })
        val fixedVisible = source.categories.flatMap { it.members }.map { it.tile }.toSet()
        assertEquals(4, source.decoyRelations.first().members.count { it.tile in fixedVisible })
        val result = ConnectionsGenerator(source).generate(0, profile(source))
        assertTrue(result.toString(), result is ConnectionsGenerationResult.NoCandidate)
        assertEquals(ConnectionsFailure.IMPOSSIBLE_CONSTRAINTS, (result as ConnectionsGenerationResult.NoCandidate).reason)
    }

    @Test fun candidateBudgetAfterRecentRejectionDoesNotClaimRecentPoolExhausted() {
        val source = pilot
        val generator = ConnectionsGenerator(source)
        val first = success(generator.generate(0, profile(source)))
        // Same seed reaches this excluded board first; a normal-budget request finds another.
        assertNotEquals(first.signature, success(generator.generate(0, profile(source), setOf(first.signature))).signature)
        val result = generator.generate(0, profile(source).copy(maxCandidates = 1), setOf(first.signature))
        assertTrue(result.toString(), result is ConnectionsGenerationResult.NoCandidate)
        val failure = result as ConnectionsGenerationResult.NoCandidate
        assertEquals(ConnectionsFailure.BUDGET_EXHAUSTED, failure.reason)
        assertEquals(1, failure.diagnostics.candidates)
        assertTrue((failure.diagnostics.rejections["recent-board"] ?: 0) >= 1)
    }

    @Test fun generatorDetachesCallerListsAndHashPinsSenseAndPolicyMetadata() {
        val original = oneBoardBank()
        val mutableMembers = original.categories.first().members.toMutableList()
        val mutableCategories = original.categories.mapIndexed { i, r -> if (i == 0) r.copy(members = mutableMembers) else r }.toMutableList()
        val source = original.copy(categories = mutableCategories)
        val pinned = profile(source)
        val generator = ConnectionsGenerator(source)
        mutableMembers.clear()
        mutableCategories.clear()
        assertIndependentBoard(original, success(generator.generate(0, pinned)))
        val changedSense = original.copy(categories = original.categories.mapIndexed { i, r ->
            if (i == 0) r.copy(members = r.members.mapIndexed { j, m -> if (j == 0) m.copy(sense = "different sense") else m }) else r
        })
        assertNotEquals(original.contentSha256, changedSense.contentSha256)
        assertNotEquals(original.contentSha256, original.copy(categories = original.categories.mapIndexed { i, r -> if (i == 0) r.copy(reviewStatus = "human-reviewed") else r }).contentSha256)
    }

    @Test fun cancellationWorksBeforeAndWithinSearchWithoutPoisoningNextRequest() {
        val source = pilot
        val generator = ConnectionsGenerator(source)
        assertTrue(generator.generate(0, profile(source)) { true } is ConnectionsGenerationResult.Cancelled)
        var polls = 0
        val interrupted = generator.generate(0, profile(source)) { ++polls >= 12 }
        assertTrue(interrupted.toString(), interrupted is ConnectionsGenerationResult.Cancelled)
        assertTrue(polls >= 12)
        assertTrue(success(generator.generate(0, profile(source))).diagnostics.steps > 0)
    }

    @Test fun malformedInputsAndUnpinnedProfilesCannotProduceAFalseSuccess() {
        val source = oneBoardBank()
        val first = source.categories.first()
        val malformed = listOf(
            source.copy(version = ""), source.copy(provenance = ""), source.copy(schemaVersion = 999),
            source.copy(categories = source.categories + first),
            source.copy(categories = listOf(first.copy(members = first.members + first.members.first())) + source.categories.drop(1)),
            source.copy(categories = listOf(first.copy(members = first.members.mapIndexed { i, m -> if (i == 0) m.copy(tile = "foot") else m })) + source.categories.drop(1)),
            source.copy(categories = listOf(first.copy(members = first.members.mapIndexed { i, m -> if (i == 0) m.copy(tile = "FOOT|HAND") else m })) + source.categories.drop(1)),
            source.copy(categories = listOf(first.copy(members = first.members.mapIndexed { i, m -> if (i == 0) m.copy(sense = "") else m })) + source.categories.drop(1)),
        )
        malformed.forEach { bank ->
            assertTrue(ConnectionsGenerator.validateSource(bank).isNotEmpty())
            val result = ConnectionsGenerator(bank).generate(0, profile(bank))
            assertTrue(result.toString(), result is ConnectionsGenerationResult.NoCandidate)
            assertEquals(ConnectionsFailure.INVALID_SOURCE, (result as ConnectionsGenerationResult.NoCandidate).reason)
        }
        listOf(
            profile(source).copy(bankSha256 = "wrong"), profile(source).copy(generatorVersion = "future"),
            profile(source).copy(selectionPolicy = "unknown"), profile(source).copy(requiredDecoys = 1),
            profile(source).copy(maxSteps = 0), profile(source).copy(maxCandidates = 0),
        ).forEach { invalid ->
            val result = ConnectionsGenerator(source).generate(0, invalid)
            assertTrue(result.toString(), result is ConnectionsGenerationResult.NoCandidate)
            assertEquals(ConnectionsFailure.INVALID_PROFILE, (result as ConnectionsGenerationResult.NoCandidate).reason)
        }
    }

    @Test fun frozenGeneratedAndLegacySnapshotsRoundTripWithoutTheirSourceBank() {
        val source = pilot
        val generated = success(ConnectionsGenerator(source).generate(0, profile(source))).puzzle
        val legacy = ConnectionsContent.puzzles.first()
        val state = mapOf("solved" to "0", "selected" to "9,8", "mistakes" to "4-5-6-8")
        val library = Library(listOf(generated, legacy).mapIndexed { i, puzzle ->
            Attempt("frozen-$i", "connections", PuzzleMode.EXTRA, null, puzzle, state,
                "2026-10-26T12:00:00Z", "2026-10-26T12:01:00Z", result = ConnectionsRules.result(state))
        })
        val bytes = LibraryCodec.encode(library)
        // A later source revision may be incompatible. Decoding has no dependency on it.
        val changedSource = source.copy(version = "future-bank", categories = emptyList())
        assertTrue(ConnectionsGenerator(changedSource).generate(0, profile(changedSource)) is ConnectionsGenerationResult.NoCandidate)
        val restored = LibraryCodec.decode(bytes)
        assertEquals(library, restored)
        restored.attempts.forEach { attempt ->
            assertEquals(emptyList<String>(), ConnectionsRules.validate(attempt.puzzle))
            assertEquals(setOf(0), ConnectionsRules.solved(attempt.state))
            assertEquals(setOf(9, 8), ConnectionsRules.selection(attempt.state))
            assertEquals(1, ConnectionsRules.mistakes(attempt.state).size)
            assertEquals(attempt.result, ConnectionsRules.result(attempt.state))
        }
    }

    internal fun assertIndependentBoard(source: ConnectionsBank, result: ConnectionsGenerationResult.Success) {
        val groups = ConnectionsRules.groups(result.puzzle)
        assertEquals(emptyList<String>(), ConnectionsRules.validate(result.puzzle))
        assertEquals(4, result.diagnostics.chosenCategoryIds.size)
        groups.forEachIndexed { i, group ->
            val category = source.categories.single { it.id == result.diagnostics.chosenCategoryIds[i] }
            assertEquals(category.label, group.label)
            assertTrue(group.words.all { word -> category.members.any { it.tile == word } })
        }
        val tiles = groups.flatMap { it.words }.sorted()
        val relations = source.categories + source.decoyRelations
        val quartets = relations.flatMap { relation ->
            chooseFour(relation.members.map { it.tile }.filter { it in tiles })
        }.map { it.sorted().joinToString("|") }.toSet()
        assertEquals(quartets, result.diagnostics.testedQuartets.toSet())
        val partitions = mutableSetOf<String>()
        val sets = quartets.map { it.split('|').toSet() }
        fun cover(remaining: Set<String>, chosen: List<String>) {
            if (remaining.isEmpty()) { partitions += chosen.sorted().joinToString("::"); return }
            val first = remaining.minOrNull()!!
            for (group in sets) if (first in group && remaining.containsAll(group)) cover(remaining - group, chosen + group.sorted().joinToString("|"))
        }
        cover(tiles.toSet(), emptyList())
        assertEquals(setOf(groups.map { it.words.sorted().joinToString("|") }.sorted().joinToString("::")), partitions)
        assertEquals(2, result.diagnostics.decoys.size)
        assertEquals(2, result.diagnostics.decoys.map { it.relationId }.distinct().size)
        assertEquals(2, result.diagnostics.decoys.map { it.members.map { m -> m.tile }.sorted() }.distinct().size)
        result.diagnostics.decoys.forEach { decoy ->
            val relation = relations.single { it.id == decoy.relationId }
            val matches = relation.members.filter { it.tile in tiles }
            assertEquals(3, matches.size)
            assertEquals(matches.toSet(), decoy.members.toSet())
            assertEquals(relation.label, decoy.label)
            val spanned = groups.count { group -> group.words.any { word -> matches.any { it.tile == word } } }
            assertTrue(spanned >= 2)
            assertEquals(spanned, decoy.groupsSpanned)
        }
    }

    private fun chooseFour(words: List<String>): List<List<String>> = buildList {
        for (a in 0 until words.size - 3) for (b in a + 1 until words.size - 2)
            for (c in b + 1 until words.size - 1) for (d in c + 1 until words.size)
                add(listOf(words[a], words[b], words[c], words[d]))
    }
}
