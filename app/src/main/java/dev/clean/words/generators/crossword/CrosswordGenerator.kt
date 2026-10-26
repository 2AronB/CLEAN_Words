package dev.clean.words.generators.crossword

import dev.clean.words.core.PuzzleRecord
import java.security.MessageDigest
import java.util.Random

data class CrosswordClue(val id: String, val text: String, val sense: String, val style: String = "definition", val difficulty: String = "uncalibrated", val provenance: String = "original-clean-words-2026-10-26", val review: String = "ai-draft-needs-human-review")
data class CrosswordAnswer(val id: String, val text: String, val sense: String, val clues: List<CrosswordClue>, val dialect: String = "general-American-English", val familiarity: String = "AI-familiar-unrated", val allowedness: String = "allowed", val review: String = "ai-draft-needs-human-review") { val length: Int get() = text.length }
data class CrosswordTemplate(val id: String, val width: Int, val height: Int, val mask: String)
data class CrosswordBank(val version: String, val provenance: String, val answers: List<CrosswordAnswer>, val templates: List<CrosswordTemplate>, val sourceSha256: String = "") {
    val contentSha256: String get() = cwHash(buildList {
        addAll(listOf(version,provenance))
        answers.forEach { a -> addAll(listOf("answer",a.id,a.text,a.sense,a.dialect,a.familiarity,a.allowedness,a.review));a.clues.forEach { c ->addAll(listOf("clue",c.id,c.text,c.sense,c.style,c.difficulty,c.provenance,c.review)) } }
        templates.forEach {t->addAll(listOf("template",t.id,t.width.toString(),t.height.toString(),t.mask))}
    }.joinToString("") { "${it.length}:$it" })
}
data class CrosswordProfile(val width: Int = 7, val height: Int = 7, val maxSteps: Int = 250_000, val maxCandidates: Int = 32)
enum class CrosswordFailure { INVALID_SOURCE, INVALID_PROFILE, IMPOSSIBLE_CONSTRAINTS, RECENT_POOL_EXHAUSTED, BUDGET_EXHAUSTED }
data class CrosswordDiagnostics(val steps: Int, val candidates: Int, val rejections: Map<String, Int>, val templateId: String = "")
sealed interface CrosswordGenerationResult {
    data class Success(val puzzle: PuzzleRecord, val signature: String, val diagnostics: CrosswordDiagnostics) : CrosswordGenerationResult
    data class NoCandidate(val reason: CrosswordFailure, val diagnostics: CrosswordDiagnostics) : CrosswordGenerationResult
    data class Cancelled(val diagnostics: CrosswordDiagnostics) : CrosswordGenerationResult
}
data class CrosswordSlot(val number: Int, val direction: String, val cells: List<Int>)

/** CPU-only bounded crossing search; input order and Java Random are pinned by POLICY. */
class CrosswordGenerator(bank: CrosswordBank) {
    private val source = bank.copy(answers = bank.answers.map { it.copy(clues = it.clues.toList()) }, templates = bank.templates.toList())
    private val sourceErrors = validateSource(source)
    private val bankHash = source.contentSha256
    private val clueHashes = source.answers.flatMap {it.clues}.associate {it.id to cwHash(it.text)}
    companion object {
        const val VERSION = "crossword-crossing-dfs-v1"
        const val POLICY = "original-pilot-mrv-java-random-7x7-v1"
        fun slots(template: CrosswordTemplate): List<CrosswordSlot> {
            val w = template.width; val h = template.height
            if (w !in 3..15 || h !in 3..15 || template.mask.length != w*h) return emptyList()
            val raw = mutableListOf<Pair<String,List<Int>>>()
            for (i in template.mask.indices) if (template.mask[i] != '#') {
                if (i%w == 0 || template.mask[i-1] == '#') {
                    val cells = (i until (i/w+1)*w).takeWhile { template.mask[it] != '#' }
                    if (cells.size > 1) raw += "A" to cells
                }
                if (i < w || template.mask[i-w] == '#') {
                    val cells = (i until w*h step w).takeWhile { template.mask[it] != '#' }
                    if (cells.size > 1) raw += "D" to cells
                }
            }
            val numbers = raw.map { it.second.first() }.distinct().sorted().withIndex().associate { it.value to it.index+1 }
            return raw.map { CrosswordSlot(numbers.getValue(it.second.first()),it.first,it.second) }
        }
        fun validateTemplate(t: CrosswordTemplate): List<String> = buildList {
            if (t.width !in listOf(5,7) || t.height != t.width || t.mask.length != t.width*t.height || t.mask.any { it != '#' && it != '.' }) { add("Invalid dimensions/mask"); return@buildList }
            val s = slots(t); val open = t.mask.indices.filter { t.mask[it] != '#' }.toSet()
            if (s.isEmpty() || s.any { it.cells.size !in 3..7 }) add("Invalid word length")
            if (s.flatMap { it.cells }.toSet() != open) add("Unclued cell")
            if (s.map { it.direction }.toSet().size != 2) add("Both directions required")
            val seen = mutableSetOf<Int>(); val queue = ArrayDeque<Int>()
            open.firstOrNull()?.let { seen.add(it); queue.add(it) }
            while (queue.isNotEmpty()) { val i=queue.removeFirst(); val w=t.width
                listOfNotNull((i-w).takeIf { i>=w },(i+w).takeIf { i<t.mask.length-w },(i-1).takeIf { i%w>0 },(i+1).takeIf { i%w<w-1 }).filter { it in open && seen.add(it) }.forEach(queue::addLast)
            }
            if (seen != open) add("Disconnected grid")
        }
        fun validateSource(bank: CrosswordBank): List<String> = buildList {
            if (bank.version.isBlank() || bank.provenance.isBlank()) add("Missing source identity")
            if (bank.answers.isEmpty() || bank.answers.size > 5000 || bank.templates.isEmpty() || bank.templates.size > 100) { add("Source bounds"); return@buildList }
            if (bank.answers.map { it.id }.distinct().size != bank.answers.size || bank.answers.map { it.text }.distinct().size != bank.answers.size) add("Duplicate answer")
            val clues = bank.answers.flatMap { it.clues }
            if (clues.map { it.id }.distinct().size != clues.size) add("Duplicate clue ID")
            for (a in bank.answers) {
                if (!a.text.matches(Regex("[A-Z]{3,7}")) || !a.id.matches(Regex("[a-z0-9-]+")) || a.sense.isBlank() || a.sense.any {it in "|\n\r\t"} || a.allowedness != "allowed" || a.clues.isEmpty()) add("Invalid answer ${a.id}")
                if (a.clues.any { !it.id.matches(Regex("[a-z0-9-]+")) || it.text.isBlank() || it.text.length>300 || it.text.any { c -> c in "|\n\r\t" } || it.sense != a.sense || it.provenance.isBlank() }) add("Invalid clue/sense ${a.id}")
                if(a.clues.map {it.text}.distinct().size != a.clues.size) add("Duplicate clue text ${a.id}")
            }
            if (bank.templates.map { it.id }.distinct().size != bank.templates.size) add("Duplicate layout")
            bank.templates.forEach { addAll(validateTemplate(it)) }
        }
        /** Signature includes frozen clues and answer tokens; legacy snapshots are reconstructed. Latest board must be first in recent Set iteration. */
        fun semanticSignature(puzzle: PuzzleRecord): String {
            val grid=puzzle.data["grid"].orEmpty().replace("\n","").replace("\r","")
            val w=puzzle.data["width"]?.toIntOrNull() ?: 5; val h=puzzle.data["height"]?.toIntOrNull() ?: 5
            require(w in listOf(5,7) && h==w && grid.length==w*h && grid.all { it=='#'||it in 'A'..'Z' })
            val clues=puzzle.data["clues"].orEmpty().lineSequence().filter { it.contains('|') }.map { it.substringBefore('|') to it.substringAfter('|') }.toMap()
            val answers=slots(CrosswordTemplate("snapshot",w,h,grid)).map { s -> s.cells.map { grid[it] }.joinToString("") }.sorted()
            require(answers.all { !clues[it].isNullOrBlank() })
            return "cw1|${w}x$h|${cwHash(grid)}|${answers.joinToString(",")}|${answers.map { cwHash(clues.getValue(it)) }.joinToString(",")}"
        }
    }

    fun generate(seed: Long, profile: CrosswordProfile = CrosswordProfile(), recentSignatures: Set<String> = emptySet(), cancelled: () -> Boolean = { false }): CrosswordGenerationResult {
        var steps=0; var candidates=0; val rejected=mutableMapOf<String,Int>(); var chosenTemplate=""
        fun diag()=CrosswordDiagnostics(steps,candidates,rejected.toMap(),chosenTemplate)
        fun reject(reason:String) { rejected[reason]=(rejected[reason] ?: 0)+1 }
        if (cancelled()) return CrosswordGenerationResult.Cancelled(diag())
        if (sourceErrors.isNotEmpty()) return CrosswordGenerationResult.NoCandidate(CrosswordFailure.INVALID_SOURCE,diag())
        if (profile.width !in listOf(5,7) || profile.height != profile.width || profile.maxSteps !in 1..2_000_000 || profile.maxCandidates !in 1..100) return CrosswordGenerationResult.NoCandidate(CrosswordFailure.INVALID_PROFILE,diag())
        val random=Random(seed); val templates=source.templates.filter { it.width==profile.width && it.height==profile.height }.shuffled(random)
        val recentAnswers=recentSignatures.flatMap { it.split('|').getOrNull(3).orEmpty().split(',') }.toSet()
        val recentClues=recentSignatures.flatMap { it.split('|').getOrNull(4).orEmpty().split(',') }.toSet()
        val lastClues=recentSignatures.firstOrNull()?.split('|')?.getOrNull(4).orEmpty().split(',').toSet()
        val recentFills=recentSignatures.mapNotNull {it.split('|').getOrNull(2)}.toSet()
        val byLength=source.answers.filter { a -> a.clues.any { clueHashes.getValue(it.id) !in lastClues } }.groupBy { it.text.length }
        var completed: CrosswordGenerationResult.Success?=null; var wasCancelled=false
        for (template in templates.take(profile.maxCandidates)) {
            if (cancelled()) { wasCancelled=true; break }; candidates++; chosenTemplate=template.id
            val slots=slots(template); val grid=template.mask.toCharArray(); val selected=arrayOfNulls<CrosswordAnswer>(slots.size); val used=mutableSetOf<String>()
            val ranked=byLength.mapValues { (_,answers) -> answers.shuffled(random).sortedBy { if(it.text in recentAnswers)1 else 0 } }
            fun search(): Boolean {
                if (cancelled()) { wasCancelled=true;return false }
                if (steps >= profile.maxSteps) return false
                steps++
                if (selected.all { it!=null }) {
                    if(cwHash(grid.concatToString()) in recentFills) {reject("recent-fill");return false}
                    val chosen=selected.map { a -> a!!.clues.filter { clueHashes.getValue(it.id) !in lastClues }.shuffled(random).minBy { if(clueHashes.getValue(it.id) in recentClues)1 else 0 } }
                    val clues=selected.indices.joinToString("\n") { "${selected[it]!!.text}|${chosen[it].text}" }
                    val data=mapOf("width" to template.width.toString(),"height" to template.height.toString(),"grid" to grid.concatToString(),"clues" to clues,
                        "rulePolicy" to "crossword-v1","generator" to VERSION,"generatorVersion" to VERSION,"selectionPolicy" to POLICY,"sourceVersion" to source.version,"sourceSha256" to source.sourceSha256.ifEmpty {bankHash},"bankSha256" to bankHash,"templateId" to template.id,"seed" to seed.toString(),
                        "profile" to "crossword-${profile.width}x${profile.height}-v1","origin" to source.provenance,"review" to "ai-draft-needs-human-review","difficulty" to "Uncalibrated original pilot",
                        "entryMetadata" to selected.indices.joinToString("\n") { "${slots[it].number}|${slots[it].direction}|${selected[it]!!.id}|${chosen[it].id}|${selected[it]!!.sense}" })
                    val p=PuzzleRecord(id="crossword-$seed-${cwHash(grid.concatToString()+clues).take(16)}",revision=2,title="Generated ${template.width}×${template.height} crossword",data=data)
                    val signature=semanticSignature(p)
                    if (signature in recentSignatures) {reject("recent-board"); return false}
                    completed=CrosswordGenerationResult.Success(p,signature,diag()); return true
                }
                var best=-1; var options=emptyList<CrosswordAnswer>()
                for (i in slots.indices) if (selected[i]==null) {
                    val cells=slots[i].cells
                    val fits=ranked[cells.size].orEmpty().filter { a -> a.text !in used && cells.indices.all { k -> grid[cells[k]]=='.' || grid[cells[k]]==a.text[k] } }
                    if(fits.isEmpty()) {reject("crossing-dead-end");return false}
                    if(best<0 || fits.size<options.size) {best=i;options=fits}
                }
                for(a in options) {
                    if(steps>=profile.maxSteps || wasCancelled) return false
                    val cells=slots[best].cells; val before=cells.map { grid[it] }
                    cells.forEachIndexed { k,i -> grid[i]=a.text[k] };selected[best]=a;used.add(a.text)
                    if(search())return true
                    cells.forEachIndexed { k,i -> grid[i]=before[k] };selected[best]=null;used.remove(a.text)
                }
                return false
            }
            if(search())return completed!!
            if(wasCancelled || steps>=profile.maxSteps)break
            reject("unfillable-template")
        }
        return if(wasCancelled) CrosswordGenerationResult.Cancelled(diag()) else CrosswordGenerationResult.NoCandidate(
            if(steps>=profile.maxSteps || templates.size>profile.maxCandidates) CrosswordFailure.BUDGET_EXHAUSTED else if(recentSignatures.isNotEmpty()) CrosswordFailure.RECENT_POOL_EXHAUSTED else CrosswordFailure.IMPOSSIBLE_CONSTRAINTS,diag())
    }
}

private fun <T> List<T>.shuffled(random: Random): List<T> = toMutableList().also { java.util.Collections.shuffle(it,random) }
internal fun cwHash(text: String): String = MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8)).joinToString("") { (it.toInt() and 255).toString(16).padStart(2,'0') }
