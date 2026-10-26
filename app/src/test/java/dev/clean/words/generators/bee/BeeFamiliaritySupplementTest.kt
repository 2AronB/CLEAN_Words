package dev.clean.words.generators.bee

import dev.clean.words.games.bee.BeePuzzle
import dev.clean.words.games.bee.BeeOpeningQuality
import dev.clean.words.games.bee.BeeRules
import dev.clean.words.games.bee.BeeState
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class BeeFamiliaritySupplementTest {
    private val words = listOf("CENTRAL", "CARE", "LATER", "RATE", "TREE")
    private fun base() = BeeVocabulary(words, setOf("CENTRAL", "CARE"), "fixture-dictionary-v1", "fixture-editorial-v1")
    private val text = "word\nLATER\nRATE\n"
    private fun pin(source: BeeVocabulary = base(), input: String = text, count: Int = 2) = BeeFamiliarityInput(
        "fixture-additive-editorial-v2", source.sourceHash, beeSha256(input), count,
        "original AI editorial fixture; zero human review; frequency unknown")

    @Test fun supplementPinsSourceAndEvidenceAndLeavesTheEntireAcceptedDictionaryUnchanged() {
        val base = base()
        val input = pin(base)
        val prepared = BeeFamiliaritySupplement.prepare(base, text, input)
        assertEquals(base.words, prepared.words)
        assertEquals(base.byMask, prepared.byMask)
        assertEquals(base.dictionaryVersion, prepared.dictionaryVersion)
        assertEquals(base.familiarWords + setOf("LATER", "RATE"), prepared.familiarWords)
        assertEquals(input.version, prepared.familiarityVersion)
        assertEquals(beeSha256(listOf(base.sourceHash, input.supplementSha256, input.version, input.evidence).joinToString("|")), prepared.sourceHash)
        assertNotEquals(base.sourceHash, prepared.sourceHash)
        assertEquals(setOf("CENTRAL", "CARE"), base.familiarWords)
        assertEquals(prepared.sourceHash, BeeFamiliaritySupplement.prepare(base, text, input).sourceHash)
    }

    @Test fun mutableCallerInputsCannotChangePreparedSource() {
        val mutableWords = words.toMutableList()
        val mutableFamiliar = mutableSetOf("CENTRAL", "CARE")
        val source = BeeVocabulary(mutableWords, mutableFamiliar, "fixture-dictionary", "fixture-editorial")
        val prepared = BeeFamiliaritySupplement.prepare(source, text, pin(source))
        val copiedWords = prepared.words.toList()
        val copiedFamiliar = prepared.familiarWords.toSet()
        val fingerprint = prepared.sourceHash
        mutableWords.clear(); mutableFamiliar.clear()
        assertEquals(copiedWords, prepared.words)
        assertEquals(copiedFamiliar, prepared.familiarWords)
        assertEquals(fingerprint, prepared.sourceHash)
        assertThrows(UnsupportedOperationException::class.java) { (prepared.words as MutableList<String>).clear() }
        assertThrows(UnsupportedOperationException::class.java) { (prepared.familiarWords as MutableSet<String>).clear() }
    }

    @Test fun unknownBaseHashBadChecksumCountAndMissingVersionOrEvidenceAreRejected() {
        val base = base()
        val pin = pin(base)
        listOf(
            pin.copy(baseSourceHash = "other-base"),
            pin.copy(supplementSha256 = "bad-checksum"),
            pin.copy(addedWordCount = 1),
            pin.copy(addedWordCount = -1),
            pin.copy(addedWordCount = 200001),
            pin.copy(version = " "),
            pin.copy(evidence = ""),
        ).forEach { invalid ->
            assertThrows(IllegalArgumentException::class.java) { BeeFamiliaritySupplement.prepare(base, text, invalid) }
        }
        assertEquals(words.sorted(), base.words)
        assertEquals(setOf("CENTRAL", "CARE"), base.familiarWords)
    }

    @Test fun malformedDuplicateUnknownAndAlreadyFamiliarEntriesAreNotSilentlyDropped() {
        val base = base()
        listOf(
            "words\nLATER\nRATE\n", // wrong header
            "word\nLATER\nRATE", // missing LF terminator
            "word\r\nLATER\r\nRATE\r\n", // exact pinned LF format
            "word\nLATER\nLATER\n",
            "word\nlater\nRATE\n",
            "word\nLATER \nRATE\n",
            "word\nLATER\n\n",
            "word\nUNKNOWN\nRATE\n",
            "word\nCENTRAL\nRATE\n",
            "word\nCAR\nRATE\n",
        ).forEach { malformed ->
            assertThrows(IllegalArgumentException::class.java) {
                BeeFamiliaritySupplement.prepare(base, malformed, pin(base, malformed))
            }
        }
        assertEquals(setOf("CENTRAL", "CARE"), base.familiarWords)
    }

    @Test fun emptySupplementIsExplicitAndCancellationRemainsCooperative() {
        val base = base()
        val empty = "word\n"
        val prepared = BeeFamiliaritySupplement.prepare(base, empty, pin(base, empty, 0))
        assertEquals(base.words, prepared.words)
        assertEquals(base.familiarWords, prepared.familiarWords)
        assertNotEquals(base.sourceHash, prepared.sourceHash)
        assertThrows(BeePreparationCancelled::class.java) { BeeFamiliaritySupplement.prepare(base, text, pin(base)) { true } }
        var polls = 0
        assertThrows(BeePreparationCancelled::class.java) { BeeFamiliaritySupplement.prepare(base, text, pin(base)) { ++polls > 2 } }
        polls = 0
        // Polls 1..3 cover entry and both additions; subsequent polls occur during copied index preparation.
        assertThrows(BeePreparationCancelled::class.java) { BeeFamiliaritySupplement.prepare(base, text, pin(base)) { ++polls > 4 } }
    }

    @Test fun futureEvidenceAndVersionPinsChangeNewPuzzleIdentityWhileOldSnapshotStaysFrozen() {
        val base = base()
        val pin = pin(base)
        val prepared = BeeFamiliaritySupplement.prepare(base, text, pin)
        val changedVersion = BeeFamiliaritySupplement.prepare(base, text, pin.copy(version = "fixture-additive-v3"))
        val changedEvidence = BeeFamiliaritySupplement.prepare(base, text, pin.copy(evidence = "fixture frequency-backed evidence-v1"))
        assertEquals(prepared.words, changedVersion.words)
        assertEquals(prepared.familiarWords, changedVersion.familiarWords)
        assertNotEquals(prepared.sourceHash, changedVersion.sourceHash)
        assertNotEquals(prepared.sourceHash, changedEvidence.sourceHash)
        val profile = BeeProfile("fixture-identity", minFamiliarWords = 1, minAcceptedWords = 1,
            maxAcceptedWords = 100, minFamiliarLengthBuckets = 1, requireFamiliarFour = false, maxCandidateAttempts = 7)
        fun generate(source: BeeVocabulary) = (BeeGenerator(source).generate(BeeRequest(0, profile)) as BeeGenerationResult.Success).puzzle
        val saved = generate(prepared)
        val originalBoard = BeePuzzle.from(saved)
        assertNotEquals(saved.id, generate(changedVersion).id)
        assertNotEquals(saved.id, generate(changedEvidence).id)
        assertEquals(saved, generate(prepared))
        assertEquals(originalBoard, BeePuzzle.from(saved))
        assertEquals(prepared.familiarityVersion, saved.data["familiarityVersion"])
        assertEquals(prepared.sourceHash, saved.data["sourceIndexSha256"])
    }

    @Test fun realPinnedSupplementKeepsFullEnableAcceptanceAndOldSnapshotsWhileOpeningRemainsGated() {
        fun asset(path: String) = sequenceOf(File("src/main/assets/$path"), File("app/src/main/assets/$path"))
            .first { it.isFile }.readText(Charsets.UTF_8)
        val base = BeeVocabulary.fromBundledIndex(asset(BeeBundledSource.indexPath))
        val baseWords = base.words.toList()
        val old = (BeeGenerator(base).generate(BeeRequest(0)) as BeeGenerationResult.Success).puzzle
        val oldBoard = BeePuzzle.from(old)
        val oldState = BeeRules.submit(oldBoard, BeeState(), oldBoard.goalWords.first())
        val oldResult = BeeRules.result(oldBoard, oldState)
        val supplement = asset(BeeEditorialSupplement.path)
        val pin = BeeEditorialSupplement.input
        assertEquals("original-ai-editorial-bee-supplement-v2", pin.version)
        assertEquals("ef14949d1b720d6360d9d5e064c2199e3b85c07d014cbb8158b797592c0c6119", pin.supplementSha256)
        assertEquals(pin.supplementSha256, beeSha256(supplement))
        assertEquals(346, pin.addedWordCount)
        assertEquals(BeeBundledSource.indexSha256, pin.baseSourceHash)
        val prepared = BeeFamiliaritySupplement.prepare(base, supplement, pin)
        assertEquals(3982, prepared.familiarWords.size)
        assertEquals(3636, base.familiarWords.size)
        assertEquals(98144, prepared.words.size)
        assertEquals(baseWords, prepared.words)
        assertEquals(baseWords, base.words)
        assertEquals(base.dictionaryVersion, prepared.dictionaryVersion)
        assertEquals("89251ac1abf00f299e89fd76aa12aa3f6ef4c2d559e2b311ab70100f0128e127", BeeEditorialSupplement.compositeSourceHash)
        assertEquals(BeeEditorialSupplement.compositeSourceHash, prepared.sourceHash)
        assertEquals(pin.evidence, prepared.familiarityEvidence)
        val generator = BeeGenerator(prepared)
        assertTrue(generator.generateOpening(BeeRequest(0), null) is BeeGenerationResult.InvalidInput)
        val fixtureQuality = BeeOpeningQuality(0, "test-corpus-only-not-owner-approval")
        val generated = (generator.generateOpening(BeeRequest(0), fixtureQuality) as BeeGenerationResult.Success).puzzle
        val board = BeePuzzle.from(generated)
        val independent = asset("dictionaries/enable1.txt").lineSequence()
            .filter { it.matches(Regex("[a-z]{4,32}")) }.map { it.uppercase(java.util.Locale.ROOT) }
            .filter { word -> board.center in word && word.all { it in board.letters } }.toSet()
        assertEquals(independent, board.validWords)
        assertEquals(independent.intersect(prepared.familiarWords), board.goalWords)
        assertEquals(pin.evidence, generated.data["review"])
        assertEquals(pin.version, generated.data["familiarityVersion"])
        assertEquals(prepared.sourceHash, generated.data["sourceIndexSha256"])
        assertEquals(emptyList<String>(), BeeRules.validate(board))
        assertEquals(old, (BeeGenerator(base).generate(BeeRequest(0)) as BeeGenerationResult.Success).puzzle)
        assertEquals(oldResult, BeeRules.result(BeePuzzle.from(old), BeeState.from(oldState.toMap())))
        val futureEvidence = "fixture future evidence boundary; empirical test input only"
        val future = BeeFamiliaritySupplement.prepare(base, supplement, pin.copy(evidence = futureEvidence))
        val futurePuzzle = (BeeGenerator(future).generateOpening(BeeRequest(0), fixtureQuality) as BeeGenerationResult.Success).puzzle
        assertEquals(futureEvidence, futurePuzzle.data["review"])
        assertNotEquals(generated.id, futurePuzzle.id)
        assertEquals(board.validWords, BeePuzzle.from(futurePuzzle).validWords)
        assertEquals(generated, board.snapshot(generated.id))
    }
}
