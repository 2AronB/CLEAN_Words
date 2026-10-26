package dev.clean.words.generators.bee

/** Explicit input boundary; no implicit upgrade of the currently shipped source. */
data class BeeFamiliarityInput(
    val version: String,
    val baseSourceHash: String,
    val supplementSha256: String,
    val addedWordCount: Int,
    val evidence: String,
)

/** Optional original AI editorial draft; no human review or automatic upgrade. */
object BeeEditorialSupplement {
    const val path = "generation/bee/familiarity-supplement-v2.tsv"
    val input = BeeFamiliarityInput(
        version = "original-ai-editorial-bee-supplement-v2",
        baseSourceHash = "d9dcf290c0c057c0df9277bc1be855771336445991da6f09e60133829ba20143",
        supplementSha256 = "ef14949d1b720d6360d9d5e064c2199e3b85c07d014cbb8158b797592c0c6119",
        addedWordCount = 346,
        evidence = "original-ai-editorial; human-review-pending; frequency-unknown",
    )
    const val compositeSourceHash = "89251ac1abf00f299e89fd76aa12aa3f6ef4c2d559e2b311ab70100f0128e127"
}

object BeeFamiliaritySupplement {
    /** Augments familiarity only. Every ENABLE accepted word remains accepted. Worker-only, no IO. */
    fun prepare(base: BeeVocabulary, text: String, pin: BeeFamiliarityInput,
        isCancelled: () -> Boolean = { false }): BeeVocabulary {
        if (isCancelled()) throw BeePreparationCancelled()
        require(pin.version.isNotBlank() && pin.evidence.isNotBlank() && pin.addedWordCount in 0..200_000)
        require(pin.baseSourceHash == base.sourceHash) { "Familiarity supplement base source mismatch" }
        require(text.length <= 8 * 1024 * 1024 && beeSha256(text) == pin.supplementSha256) { "Familiarity supplement checksum mismatch" }
        require(text.startsWith("word\n") && text.endsWith('\n')) { "Malformed familiarity supplement header or terminator" }
        val additions = linkedSetOf<String>()
        val accepted = base.words.toSet()
        text.removeSuffix("\n").lineSequence().drop(1).forEach { word ->
            if (isCancelled()) throw BeePreparationCancelled()
            require(word.matches(Regex("[A-Z]{4,32}")) && word in accepted && word !in base.familiarWords && additions.add(word)) {
                "Invalid, unknown, already familiar or duplicate supplement word: $word"
            }
        }
        require(additions.size == pin.addedWordCount) { "Familiarity supplement count mismatch" }
        val fingerprint = beeSha256(listOf(base.sourceHash, pin.supplementSha256, pin.version, pin.evidence).joinToString("|"))
        return BeeVocabulary(base.words, base.familiarWords + additions, base.dictionaryVersion,
            pin.version, fingerprint, isCancelled, pin.evidence)
    }
}
