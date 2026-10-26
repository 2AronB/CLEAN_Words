package dev.clean.words.games.bee

import dev.clean.words.core.GameResult
import dev.clean.words.core.PlayStatus
import dev.clean.words.core.PuzzleRecord
import java.util.Locale
import kotlin.math.ceil

data class BeePuzzle(
    val letters: String,
    val center: Char,
    val goalWords: Set<String>,
    val validWords: Set<String>,
    val dictionaryVersion: String,
    val scoringVersion: String = LEGACY_SCORING,
    val openingData: Map<String, String> = emptyMap(),
    val snapshotErrors: List<String> = emptyList(),
) {
    val legacy: Boolean get() = scoringVersion == LEGACY_SCORING
    val opening: Boolean get() = scoringVersion == BeeOpening.SCORING
    val maximumGoalScore: Int get() = goalWords.sumOf { BeeRules.points(this, it) }
    val target: Int get() = ceil(maximumGoalScore * 0.7).toInt()
    val wordTarget: Int get() = ceil(goalWords.size * 0.7).toInt()
    fun snapshot(id: String): PuzzleRecord = PuzzleRecord(id, revision = if (opening) 3 else if (legacy) 1 else 2, title = "Seven letters · $center in the center", data = openingData + mapOf(
        "letters" to letters, "center" to center.toString(),
        "goalWords" to goalWords.sorted().joinToString(","),
        "validWords" to validWords.sorted().joinToString(","),
        "dictionaryVersion" to dictionaryVersion,
        "scoringVersion" to scoringVersion,
    ))

    companion object {
        const val LEGACY_SCORING = "bee-goal70-bonus-v1"
        const val WORD_COUNT_SCORING = "bee-wordcount-v2"
        fun from(record: PuzzleRecord) = BeePuzzle(
            letters = record.data["letters"].orEmpty(),
            center = record.data["center"].orEmpty().singleOrNull() ?: '?',
            goalWords = record.data["goalWords"].orEmpty().split(',').filter { it.isNotEmpty() }.toSet(),
            validWords = record.data["validWords"].orEmpty().split(',').filter { it.isNotEmpty() }.toSet(),
            dictionaryVersion = record.data["dictionaryVersion"].orEmpty(),
            scoringVersion = record.data["scoringVersion"] ?: LEGACY_SCORING,
            openingData = if (record.data["scoringVersion"] == BeeOpening.SCORING) record.data.filterKeys {
                it !in setOf("letters", "center", "goalWords", "validWords", "dictionaryVersion", "scoringVersion")
            } else emptyMap(),
            snapshotErrors = if (record.revision >= 3 && record.data["scoringVersion"] != BeeOpening.SCORING)
                listOf("Unsupported snapshot revision/scoring policy")
            else if (record.data["scoringVersion"] == BeeOpening.SCORING) buildList {
                if (record.revision != 3) add("Unsupported opening snapshot revision")
                listOf("validWords", "goalWords").forEach { key ->
                    val words = record.data[key].orEmpty().split(',')
                    if (words.any { it.isEmpty() } || words.distinct().size != words.size) add("Malformed frozen $key")
                }
            } else emptyList(),
        )
    }
}

data class BeeLengthProgress(val length: Int, val found: Int, val total: Int)

/** Accumulates input immediately, including several events before a parent recomposes. */
class BeeInputBuffer(initial: BeeState, private val ordered: Boolean = initial.statePolicy == BeeOpening.STATE) {
    private fun normalize(state: BeeState): BeeState =
        if (ordered && state.statePolicy == null && state.acceptedOrder == null && state.found.isEmpty() && state.decodeError == null)
            state.copy(statePolicy = BeeOpening.STATE, acceptedOrder = emptyList()) else state

    var current: BeeState = normalize(initial)
        private set
    private var observed = current
    private val pending = mutableListOf<BeeState>()

    fun edit(transform: (BeeState) -> BeeState): BeeState {
        val next = transform(current)
        current = if (ordered) next.copy(editRevision = Math.addExact(current.editRevision, 1)) else next
        pending += current
        return current
    }

    fun acceptExternal(externalState: BeeState): BeeState {
        val external = normalize(externalState)
        if (external == observed) return current
        observed = external
        // Ordered accepted submissions are append-only, even if a late parent value arrives.
        val localOrder = current.acceptedOrder
        val externalOrder = external.acceptedOrder
        if (current.statePolicy == BeeOpening.STATE && external.decodeError == null && localOrder != null &&
            (externalOrder ?: emptyList()).size < localOrder.size &&
            localOrder.take(externalOrder.orEmpty().size) == externalOrder.orEmpty()) return current
        if (current.statePolicy == BeeOpening.STATE && localOrder != null && externalOrder != null &&
            localOrder.take(minOf(localOrder.size, externalOrder.size)) != externalOrder.take(minOf(localOrder.size, externalOrder.size))) {
            current = external.copy(decodeError = "Conflicting accepted submission order; recovery required", originalMap = external.toMap())
            pending.clear()
            return current
        }
        if (ordered && external.decodeError == null && external.statePolicy == BeeOpening.STATE &&
            externalOrder == localOrder && external.editRevision < current.editRevision) return current
        val acknowledged = pending.indexOfLast { it == external }
        if (acknowledged >= 0) {
            repeat(acknowledged + 1) { pending.removeAt(0) }
            // An older parent acknowledgement must not roll back a newer local edit.
            if (pending.isEmpty()) current = external
        } else {
            pending.clear()
            current = external
        }
        return current
    }
}

data class BeeState(val found: Set<String> = emptySet(), val entry: String = "", val message: String = "",
    val acceptedOrder: List<String>? = null, val statePolicy: String? = null, val decodeError: String? = null,
    val originalMap: Map<String, String>? = null, val editRevision: Long = 0) {
    fun toMap(): Map<String, String> {
        originalMap?.let { return it }
        return if (statePolicy != null || acceptedOrder != null) mapOf(
            "statePolicy" to (statePolicy ?: ""), "acceptedOrder" to acceptedOrder.orEmpty().joinToString(","),
            "entry" to entry, "message" to message, "editRevision" to editRevision.toString())
        else mapOf("found" to found.sorted().joinToString(","), "entry" to entry, "message" to message)
    }
    companion object {
        fun from(state: Map<String, String>): BeeState {
            val ordered = "acceptedOrder" in state || "statePolicy" in state || "editRevision" in state
            val order = if ("acceptedOrder" in state) state.getValue("acceptedOrder").let { if (it.isEmpty()) emptyList() else it.split(',') } else null
            val oldFound = state["found"].orEmpty().split(',').filter { it.isNotEmpty() }.toSet()
            val revision = if ("editRevision" in state) state["editRevision"]?.toLongOrNull() else 0L
            val error = when {
                revision == null || revision < 0 || revision == Long.MAX_VALUE -> "Unsupported or malformed input edit revision"
                ordered && (state["statePolicy"] != BeeOpening.STATE || order == null) -> "Unsupported or malformed ordered state"
                order != null && (order.any { it.isBlank() } || order.distinct().size != order.size) -> "Malformed or duplicate ordered state"
                order != null && "found" in state && oldFound != order.toSet() -> "Ordered state conflicts with found words"
                else -> null
            }
            return BeeState(if (ordered) order?.toSet() ?: oldFound else oldFound, state["entry"].orEmpty(), state["message"].orEmpty(),
                order, state["statePolicy"], error, if (error != null) state.toMap() else null, revision ?: 0)
        }
    }
}

object BeeRules {
    fun foundWords(puzzle: BeePuzzle, state: BeeState): Set<String> = state.found.intersect(puzzle.validWords)

    fun lengthProgress(puzzle: BeePuzzle, state: BeeState): List<BeeLengthProgress> {
        val found = foundWords(puzzle, state)
        return puzzle.validWords.groupBy(String::length).toSortedMap().map { (length, words) ->
            BeeLengthProgress(length, words.count { it in found }, words.size)
        }
    }

    fun totalScore(puzzle: BeePuzzle, state: BeeState): Int = foundWords(puzzle, state).sumOf { points(puzzle, it) }
    fun pangram(puzzle: BeePuzzle, word: String): Boolean =
        word.toSet() == puzzle.letters.toSet()

    fun points(puzzle: BeePuzzle, word: String): Int =
        (if (word.length == 4) 1 else word.length) + if (pangram(puzzle, word)) 7 else 0

    fun wordError(puzzle: BeePuzzle, word: String): String? = when {
        word.length < 4 -> "Use at least four letters."
        word.any { it !in 'A'..'Z' } -> "Use the displayed English letters only."
        puzzle.center !in word -> "Every word needs the center letter ${puzzle.center}."
        word.any { it !in puzzle.letters } -> "Use only the seven displayed letters. Letters may repeat."
        word !in puzzle.validWords -> "That word is not in this puzzle's bundled dictionary. No points lost."
        else -> null
    }

    fun goalScore(puzzle: BeePuzzle, state: BeeState): Int =
        state.found.filter { it in puzzle.goalWords }.sumOf { points(puzzle, it) }

    fun bonusScore(puzzle: BeePuzzle, state: BeeState): Int =
        state.found.filter { it !in puzzle.goalWords && it in puzzle.validWords }.sumOf { points(puzzle, it) }

    fun submit(puzzle: BeePuzzle, state: BeeState, submitted: String = state.entry): BeeState {
        if (puzzle.snapshotErrors.isNotEmpty()) return state.copy(message = "Unsupported or malformed saved puzzle. Progress preserved.")
        if (state.decodeError != null || (!puzzle.opening && (state.statePolicy != null || state.acceptedOrder != null)))
            return state.copy(message = "Unsupported or malformed saved state. Progress preserved.")
        if (puzzle.scoringVersion !in setOf(BeePuzzle.LEGACY_SCORING, BeePuzzle.WORD_COUNT_SCORING, BeeOpening.SCORING))
            return state.copy(message = "Unsupported scoring policy. Saved progress preserved.")
        if (puzzle.opening) {
            val errors = validate(puzzle) + BeeOpening.stateErrors(puzzle, state)
            if (errors.isNotEmpty()) return state.copy(message = "Saved opening cannot be edited: ${errors.joinToString()}. Progress preserved.")
        }
        val word = submitted.trim().uppercase(Locale.ROOT)
        val error = wordError(puzzle, word)
        if (error != null) return state.copy(message = error)
        if (word in state.found) return state.copy(message = "You already found $word. No extra points.")
        val next = state.copy(found = state.found + word, entry = "",
            acceptedOrder = if (puzzle.opening) state.acceptedOrder.orEmpty() + word else state.acceptedOrder,
            statePolicy = if (puzzle.opening) BeeOpening.STATE else state.statePolicy)
        if (puzzle.opening) {
            val progress = BeeOpening.progress(puzzle, next)
            return next.copy(message = (if (pangram(puzzle, word)) "Pangram! " else "") + "$word: +${points(puzzle, word)} points. " +
                (if (progress.recorded) "Opening recorded: ${progress.percent.toPlainString()}%. Continued words add completion and all-word points." else "Opening ${progress.words.size}/10 · provisional ${progress.percent.toPlainString()}%. ") +
                if (progress.goalReached && !progress.recorded) " Completion goal reached; opening still unfinished." else "")
        }
        if (!puzzle.legacy) return next.copy(message =
            (if (pangram(puzzle, word)) "Pangram! " else "") + "$word: +${points(puzzle, word)} points." +
                if (foundWords(puzzle, next).size >= puzzle.wordTarget) " Daily goal reached; keep finding words if you like." else "")
        // Preserve legacy result semantics, but avoid presenting two categories of words.
        val label = "points"
        return next.copy(message = (if (pangram(puzzle, word)) "Pangram! " else "") +
            "$word: +${points(puzzle, word)} $label." +
            if (goalScore(puzzle, next) >= puzzle.target) " Target reached; keep finding words if you like." else "")
    }

    fun result(puzzle: BeePuzzle, state: BeeState): GameResult {
        val policyErrors = when {
            puzzle.snapshotErrors.isNotEmpty() -> puzzle.snapshotErrors
            state.decodeError != null -> listOf(state.decodeError)
            !puzzle.opening && (state.statePolicy != null || state.acceptedOrder != null) -> listOf("Ordered state requires its original opening policy")
            puzzle.opening -> validate(puzzle) + BeeOpening.stateErrors(puzzle, state)
            puzzle.scoringVersion !in setOf(BeePuzzle.LEGACY_SCORING, BeePuzzle.WORD_COUNT_SCORING) -> listOf("Unsupported scoring version")
            else -> emptyList()
        }
        if (policyErrors.isNotEmpty()) return GameResult(summary = "Saved Bee requires recovery: ${policyErrors.joinToString()}. Progress preserved.")
        if (puzzle.opening) {
            val p = BeeOpening.progress(puzzle, state)
            // Shared status remains completion only. Opening score is available through the pure API.
            return GameResult(status = if (p.goalReached) PlayStatus.COMPLETED else PlayStatus.IN_PROGRESS,
                summary = "${if (p.recorded) "Recorded opening" else "Provisional opening ${p.words.size}/10"}: ${p.percent.toPlainString()}% · Completion ${p.foundCount}/${p.completionGoal} words · ${p.allWordPoints} all-word points")
        }
        if (!puzzle.legacy) {
            val count = foundWords(puzzle, state).size
            val complete = puzzle.wordTarget > 0 && count >= puzzle.wordTarget
            return GameResult(
                status = if (complete) PlayStatus.COMPLETED else PlayStatus.IN_PROGRESS,
                summary = "${if (complete) "Daily goal reached" else "Daily goal"}: $count/${puzzle.wordTarget} words · ${totalScore(puzzle, state)} points",
                score = count, maxScore = puzzle.wordTarget,
            )
        }
        val score = goalScore(puzzle, state)
        val bonus = bonusScore(puzzle, state)
        val complete = puzzle.target > 0 && score >= puzzle.target
        return GameResult(
            status = if (complete) PlayStatus.COMPLETED else PlayStatus.IN_PROGRESS,
            summary = "${if (complete) "Target reached" else "Goal"}: $score/${puzzle.target} points · $bonus bonus points",
            score = score, maxScore = puzzle.maximumGoalScore,
        )
    }

    fun validate(puzzle: BeePuzzle): List<String> = buildList {
        addAll(puzzle.snapshotErrors)
        if (puzzle.letters.length != 7 || puzzle.letters.toSet().size != 7 || puzzle.letters.any { it !in 'A'..'Z' }) add("Exactly seven distinct uppercase letters required")
        if (puzzle.center !in puzzle.letters) add("Center must be one of the seven letters")
        if (puzzle.dictionaryVersion.isBlank()) add("Missing pinned dictionary version")
        if (puzzle.scoringVersion !in setOf(BeePuzzle.LEGACY_SCORING, BeePuzzle.WORD_COUNT_SCORING, BeeOpening.SCORING)) add("Unsupported scoring version")
        if (puzzle.opening) addAll(BeeOpening.puzzleErrors(puzzle))
        if (puzzle.goalWords.isEmpty()) add("Missing goal words")
        if (!puzzle.validWords.containsAll(puzzle.goalWords)) add("Every goal word must be dictionary accepted")
        if (puzzle.goalWords.none { pangram(puzzle, it) }) add("At least one goal pangram required")
        puzzle.validWords.forEach { word -> if (wordError(puzzle, word) != null) add("Invalid accepted word: $word") }
        if (puzzle.target <= 0 || puzzle.target > puzzle.maximumGoalScore) add("Target must be attainable")
        if (puzzle.wordTarget <= 0 || puzzle.wordTarget > puzzle.validWords.size) add("Word-count target must be attainable")
    }
}
