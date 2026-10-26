package dev.clean.words.generators.connections

import dev.clean.words.core.PuzzleRecord
import java.security.MessageDigest
import java.util.Collections
import java.util.Random

data class ConnectionsMember(val tile: String, val sense: String)

data class ConnectionsRelation(
    val id: String,
    val label: String,
    val explanation: String,
    val members: List<ConnectionsMember>,
    val relationType: String = "semantic",
    val prerequisites: String = "Everyday American English",
    val wordplay: Boolean = false,
    val revision: Int = 1,
    val reviewStatus: String = "ai-draft-needs-human-review",
)

data class ConnectionsBank(
    val version: String,
    val provenance: String,
    val categories: List<ConnectionsRelation>,
    val decoyRelations: List<ConnectionsRelation>,
    val schemaVersion: Int = 1,
    val sourceSha256: String = "",
) {
    /** Canonical typed-input hash, distinct from the upstream JSON bytes hash. */
    val contentSha256: String get() {
        fun relation(r: ConnectionsRelation) = fields(listOf(r.id, r.label, r.explanation,
            r.relationType, r.prerequisites, r.wordplay.toString(), r.revision.toString(), r.reviewStatus) +
            r.members.sortedBy { it.tile }.flatMap { listOf(it.tile, it.sense) })
        return sha256(fields(listOf(schemaVersion.toString(), version, provenance, sourceSha256,
            fields(categories.sortedBy { it.id }.map(::relation)),
            fields(decoyRelations.sortedBy { it.id }.map(::relation)))))
    }
}

data class ConnectionsProfile(
    val bankSha256: String,
    val maxSteps: Int = 250_000,
    val maxCandidates: Int = 500,
    val generatorVersion: String = ConnectionsGenerator.VERSION,
    val selectionPolicy: String = ConnectionsGenerator.POLICY,
    val requiredDecoys: Int = 2,
)

enum class ConnectionsFailure {
    INVALID_SOURCE, INVALID_PROFILE, IMPOSSIBLE_CONSTRAINTS, RECENT_POOL_EXHAUSTED, BUDGET_EXHAUSTED,
}

data class ConnectionsDecoy(
    val relationId: String,
    val label: String,
    val members: List<ConnectionsMember>,
    val groupsSpanned: Int,
)

data class ConnectionsDiagnostics(
    val steps: Int,
    val candidates: Int,
    val rejections: Map<String, Int>,
    val chosenCategoryIds: List<String> = emptyList(),
    val decoys: List<ConnectionsDecoy> = emptyList(),
    val testedQuartets: List<String> = emptyList(),
)

sealed interface ConnectionsGenerationResult {
    data class Success(val puzzle: PuzzleRecord, val signature: String, val diagnostics: ConnectionsDiagnostics) : ConnectionsGenerationResult
    data class NoCandidate(val reason: ConnectionsFailure, val diagnostics: ConnectionsDiagnostics) : ConnectionsGenerationResult
    data class Cancelled(val diagnostics: ConnectionsDiagnostics) : ConnectionsGenerationResult
}

/** CPU-only module. No default bundled bank and no runtime adapter in slice 2. */
class ConnectionsGenerator(bank: ConnectionsBank) {
    // Reject oversized sources before copying, then detach all accepted caller-owned lists.
    private val sourceErrors = validateSource(bank)
    private val source = if (sourceErrors.isEmpty()) bank.copy(
        categories = bank.categories.sortedBy { it.id }.map { it.copy(members = it.members.sortedBy { m -> m.tile }) },
        decoyRelations = bank.decoyRelations.sortedBy { it.id }.map { it.copy(members = it.members.sortedBy { m -> m.tile }) },
    ) else null

    companion object {
        const val VERSION = "connections-decoy-composer-v1"
        const val POLICY = "two-triples-java-random-dfs-v1"

        fun semanticSignature(puzzle: PuzzleRecord): String {
            val groups = (0..3).map { puzzle.data["group.$it.words"].orEmpty().split('|') }
            require(groups.all { g -> g.size == 4 && g.all { it.matches(Regex("[A-Z]+(?: [A-Z]+)*")) } })
            require(groups.flatten().distinct().size == 16)
            return signature(groups)
        }

        fun validateSource(bank: ConnectionsBank): List<String> = buildList {
            if (bank.schemaVersion != 1) add("Unsupported schema")
            if (bank.version.isBlank() || bank.provenance.isBlank() || bank.version.length > 128 || bank.provenance.length > 4096)
                add("Missing or oversized source identity/provenance")
            if (bank.sourceSha256.isNotEmpty() && !bank.sourceSha256.matches(Regex("[a-f0-9]{64}"))) add("Malformed upstream hash")
            if (bank.categories.size > 256 || bank.decoyRelations.size > 128) {
                add("Source exceeds category/relation bounds"); return@buildList
            }
            val relations = bank.categories + bank.decoyRelations
            if (relations.map { it.id }.distinct().size != relations.size) add("Duplicate relationship ID")
            for (r in relations) {
                if (!r.id.matches(Regex("[a-z][a-z0-9-]{0,63}")) || r.revision < 1) add("Invalid relationship identity")
                if (listOf(r.label, r.explanation, r.relationType, r.prerequisites, r.reviewStatus).any { it.isBlank() || it.length > 1024 })
                    add("Missing or oversized relationship metadata: ${r.id}")
                if (r.members.size !in 4..16) { add("Relation needs 4..16 members: ${r.id}"); continue }
                if (r.members.map { it.tile }.distinct().size != r.members.size) add("Duplicate member: ${r.id}")
                if (r.members.any { !it.tile.matches(Regex("[A-Z]+(?: [A-Z]+)*")) || it.tile.length > 64 || it.sense.isBlank() || it.sense.length > 1024 })
                    add("Malformed member/sense: ${r.id}")
            }
        }

        private fun signature(groups: List<List<String>>) =
            "connections:groups:" + groups.map { it.sorted().joinToString("|") }.sorted().joinToString("::")
    }

    fun generate(
        seed: Long,
        profile: ConnectionsProfile,
        recentSignatures: Set<String> = emptySet(),
        cancelled: () -> Boolean = { false },
    ): ConnectionsGenerationResult {
        val run = Search(seed, profile, recentSignatures, cancelled)
        if (cancelled()) return ConnectionsGenerationResult.Cancelled(run.diagnostics())
        val bank = source ?: return ConnectionsGenerationResult.NoCandidate(ConnectionsFailure.INVALID_SOURCE, run.diagnostics())
        if (profile.bankSha256 != bank.contentSha256 || profile.generatorVersion != VERSION || profile.selectionPolicy != POLICY ||
            profile.requiredDecoys != 2 || profile.maxSteps !in 1..2_000_000 || profile.maxCandidates !in 1..10_000)
            return ConnectionsGenerationResult.NoCandidate(ConnectionsFailure.INVALID_PROFILE, run.diagnostics())
        if (bank.categories.size < 4 || bank.decoyRelations.size < 2)
            return ConnectionsGenerationResult.NoCandidate(ConnectionsFailure.IMPOSSIBLE_CONSTRAINTS, run.diagnostics())
        return try {
            run.compose(bank) ?: ConnectionsGenerationResult.NoCandidate(
                if (run.excluded > 0) ConnectionsFailure.RECENT_POOL_EXHAUSTED else ConnectionsFailure.IMPOSSIBLE_CONSTRAINTS,
                run.diagnostics(),
            )
        } catch (stop: Stop) {
            if (stop.cancelled) ConnectionsGenerationResult.Cancelled(run.diagnostics())
            else ConnectionsGenerationResult.NoCandidate(ConnectionsFailure.BUDGET_EXHAUSTED, run.diagnostics())
        }
    }

    private class Stop(val cancelled: Boolean) : RuntimeException(null, null, false, false)

    private class Search(
        private val seed: Long,
        private val profile: ConnectionsProfile,
        private val recent: Set<String>,
        private val cancelled: () -> Boolean,
    ) {
        private val random = Random(seed)
        private var steps = 0
        private var candidates = 0
        var excluded = 0
            private set
        private val rejected = sortedMapOf<String, Int>()
        private var categories = emptyList<String>()
        private var decoys = emptyList<ConnectionsDecoy>()
        private var tested = emptyList<String>()
        private fun tick() {
            if (cancelled()) throw Stop(true)
            if (steps >= profile.maxSteps) throw Stop(false)
            steps++
        }
        private fun reject(reason: String) { rejected[reason] = (rejected[reason] ?: 0) + 1 }
        fun diagnostics() = ConnectionsDiagnostics(steps, candidates, rejected.toMap(), categories.toList(), decoys.toList(), tested.toList())
        private fun <T> shuffled(values: List<T>): List<T> = values.toMutableList().also { Collections.shuffle(it, random) }

        private fun combinations(values: List<String>, count: Int): List<List<String>> {
            val result = mutableListOf<List<String>>()
            fun visit(start: Int, chosen: List<String>) {
                tick()
                if (chosen.size == count) { result.add(chosen); return }
                for (i in start..values.size - (count - chosen.size)) visit(i + 1, chosen + values[i])
            }
            visit(0, emptyList())
            return result
        }

        fun compose(bank: ConnectionsBank): ConnectionsGenerationResult.Success? {
            val membership = sortedMapOf<String, MutableList<ConnectionsRelation>>()
            for (category in bank.categories) for (member in category.members) {
                tick(); membership.getOrPut(member.tile) { mutableListOf() }.add(category)
            }
            val eligible = bank.decoyRelations.filter { r -> tick(); r.members.count { it.tile in membership } >= 3 }
            val pairs = mutableListOf<Pair<ConnectionsRelation, ConnectionsRelation>>()
            for (a in eligible.indices) for (b in a + 1 until eligible.size) { tick(); pairs.add(eligible[a] to eligible[b]) }
            for ((first, second) in shuffled(pairs)) {
                tick()
                val firstTriples = shuffled(combinations(first.members.map { it.tile }.filter { it in membership }, 3))
                val secondTriples = shuffled(combinations(second.members.map { it.tile }.filter { it in membership }, 3))
                for (a in firstTriples) for (b in secondTriples) {
                    tick()
                    if (a == b) { reject("duplicate-decoy-triple"); continue }
                    val anchors = (a + b).distinct().sorted()
                    val forbidden = (first.members.map { it.tile } - a.toSet()) + (second.members.map { it.tile } - b.toSet())
                    if (anchors.any { it in forbidden }) { reject("conflicting-decoys"); continue }
                    val assigned = linkedMapOf<String, List<String>>()
                    fun assign(index: Int): ConnectionsGenerationResult.Success? {
                        tick()
                        if (index == anchors.size) {
                            if (listOf(a, b).any { triple -> assigned.values.count { words -> words.any { it in triple } } < 2 }) {
                                reject("decoy-within-one-group"); return null
                            }
                            val fixed = assigned.keys.toList().sorted()
                            val extra = bank.categories.filter { it.id !in fixed }
                            fun addGroups(start: Int, ids: List<String>): ConnectionsGenerationResult.Success? {
                                tick()
                                if (ids.size == 4) return fill(bank, ids.sorted(), assigned, anchors.toSet(), forbidden.toSet(), listOf(first, second))
                                for (i in start until extra.size) {
                                    val found = addGroups(i + 1, ids + extra[i].id)
                                    if (found != null) return found
                                }
                                return null
                            }
                            return addGroups(0, fixed)
                        }
                        val tile = anchors[index]
                        for (category in shuffled(membership.getValue(tile))) {
                            tick()
                            val old = assigned[category.id]
                            if ((old?.size ?: 0) >= 4 || (old == null && assigned.size == 4)) continue
                            assigned[category.id] = old.orEmpty() + tile
                            val found = assign(index + 1)
                            if (found != null) return found
                            if (old == null) assigned.remove(category.id) else assigned[category.id] = old
                        }
                        return null
                    }
                    val found = assign(0)
                    if (found != null) return found
                }
            }
            return null // Entire modeled search visited; budget stops escape via Stop instead.
        }

        private fun fill(
            bank: ConnectionsBank,
            ids: List<String>,
            assigned: Map<String, List<String>>,
            anchors: Set<String>,
            forbidden: Set<String>,
            selectedDecoys: List<ConnectionsRelation>,
        ): ConnectionsGenerationResult.Success? {
            val groups = ids.map { id -> bank.categories.first { it.id == id } }
            val options = groups.map { category ->
                val fixed = assigned[category.id].orEmpty()
                val available = category.members.map { it.tile }.filter { it !in anchors && it !in forbidden }
                shuffled(combinations(available, 4 - fixed.size)).map { (fixed + it).sorted() }
            }
            fun visit(index: Int, chosen: List<List<String>>, used: Set<String>): ConnectionsGenerationResult.Success? {
                tick()
                if (index == 4) return evaluate(bank, groups, chosen, selectedDecoys)
                for (option in options[index]) {
                    tick()
                    if (option.any { it in used }) { reject("duplicate-tile"); continue }
                    val found = visit(index + 1, chosen + listOf(option), used + option)
                    if (found != null) return found
                }
                return null
            }
            return visit(0, emptyList(), emptySet())
        }

        private fun evaluate(
            bank: ConnectionsBank,
            groups: List<ConnectionsRelation>,
            words: List<List<String>>,
            selectedDecoys: List<ConnectionsRelation>,
        ): ConnectionsGenerationResult.Success? {
            tick()
            if (candidates >= profile.maxCandidates) throw Stop(false)
            candidates++
            val visible = words.flatten().toSet()
            val evidence = selectedDecoys.map { relation ->
                val matches = relation.members.filter { it.tile in visible }
                ConnectionsDecoy(relation.id, relation.label, matches, words.count { g -> g.any { w -> matches.any { it.tile == w } } })
            }
            if (evidence.any { it.members.size != 3 || it.groupsSpanned < 2 } || evidence.map { it.members.map { m -> m.tile }.sorted() }.distinct().size < 2) {
                reject("insufficient-decoys"); return null
            }
            // Enumerate every four-member subset, including relations with 5+ visible members.
            val quartets = sortedSetOf<String>()
            for (relation in bank.categories + bank.decoyRelations) {
                tick()
                val matches = relation.members.map { it.tile }.filter { it in visible }
                if (matches.size >= 4) combinations(matches, 4).forEach { quartet -> tick(); quartets.add(quartet.sorted().joinToString("|")) }
            }
            val masks = quartets.map { quartet -> quartet.split('|').fold(0) { mask, tile -> mask or (1 shl words.flatten().indexOf(tile)) } }
            var partitions = 0
            fun partition(remaining: Int) {
                tick()
                if (partitions >= 2) return
                if (remaining == 0) { partitions++; return }
                val firstBit = Integer.lowestOneBit(remaining)
                for (mask in masks) {
                    tick()
                    if (mask and firstBit != 0 && mask and remaining == mask) partition(remaining xor mask)
                    if (partitions >= 2) return
                }
            }
            partition(0xffff)
            if (partitions != 1) { reject("competing-partition"); return null }
            val signature = signature(words)
            if (signature in recent) { excluded++; reject("recent-board"); return null }
            categories = groups.map { it.id }; decoys = evidence; tested = quartets.toList()
            val data = linkedMapOf(
                "origin" to bank.provenance,
                "generator" to VERSION, "selectionPolicy" to POLICY, "bankVersion" to bank.version,
                "bankSha256" to bank.contentSha256, "sourceSha256" to bank.sourceSha256,
                "rulesVersion" to "connections-four-mistakes-v1", "requiredDecoys" to profile.requiredDecoys.toString(),
                "signature" to signature, "reviewStatus" to "provisional-see-group-reviews",
                "testedQuartets" to quartets.joinToString("\n"),
            )
            groups.forEachIndexed { i, group ->
                data["group.$i.label"] = group.label
                data["group.$i.words"] = words[i].joinToString("|")
                data["group.$i.explanation"] = group.explanation + " " + words[i].joinToString("; ") { tile -> "$tile: ${group.members.first { it.tile == tile }.sense}" }
                data["group.$i.categoryId"] = group.id
                data["group.$i.revision"] = group.revision.toString()
                data["group.$i.prerequisites"] = group.prerequisites
                data["group.$i.reviewStatus"] = group.reviewStatus
            }
            evidence.forEachIndexed { i, decoy ->
                val relation = selectedDecoys.first { it.id == decoy.relationId }
                data["decoy.$i.id"] = decoy.relationId
                data["decoy.$i.label"] = decoy.label
                data["decoy.$i.members"] = decoy.members.joinToString("|") { it.tile }
                data["decoy.$i.senses"] = decoy.members.joinToString("\n") { "${it.tile}: ${it.sense}" }
                data["decoy.$i.groupsSpanned"] = decoy.groupsSpanned.toString()
                data["decoy.$i.explanation"] = relation.explanation
                data["decoy.$i.prerequisites"] = relation.prerequisites
                data["decoy.$i.reviewStatus"] = relation.reviewStatus
                data["decoy.$i.revision"] = relation.revision.toString()
            }
            tick()
            val id = "connections-generated-" + sha256(fields(listOf(VERSION, POLICY, bank.contentSha256, seed.toString(), signature)))
            return ConnectionsGenerationResult.Success(PuzzleRecord(id, 1, "Connektions", data.toMap()), signature, diagnostics())
        }
    }
}

private fun fields(values: List<String>) = values.joinToString("") { "${it.length}:$it" }
private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
