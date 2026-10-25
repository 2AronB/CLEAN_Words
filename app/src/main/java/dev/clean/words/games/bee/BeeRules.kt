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
) {
    val legacy: Boolean get() = scoringVersion == LEGACY_SCORING
    val maximumGoalScore: Int get() = goalWords.sumOf { BeeRules.points(this, it) }
    val target: Int get() = ceil(maximumGoalScore * 0.7).toInt()
    val wordTarget: Int get() = ceil(goalWords.size * 0.7).toInt()
    fun snapshot(id: String): PuzzleRecord = PuzzleRecord(id, revision = if (legacy) 1 else 2, title = "Seven letters · $center in the center", data = mapOf(
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
        )
    }
}

data class BeeLengthProgress(val length: Int, val found: Int, val total: Int)

/** Accumulates input immediately, including several events before a parent recomposes. */
class BeeInputBuffer(initial: BeeState) {
    var current: BeeState = initial
        private set
    private var observed = initial
    private val pending = mutableListOf<BeeState>()

    fun edit(transform: (BeeState) -> BeeState): BeeState {
        current = transform(current)
        pending += current
        return current
    }

    fun acceptExternal(external: BeeState): BeeState {
        if (external == observed) return current
        observed = external
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

data class BeeState(val found: Set<String> = emptySet(), val entry: String = "", val message: String = "") {
    fun toMap(): Map<String, String> = mapOf("found" to found.sorted().joinToString(","), "entry" to entry, "message" to message)
    companion object {
        fun from(state: Map<String, String>) = BeeState(
            state["found"].orEmpty().split(',').filter { it.isNotEmpty() }.toSet(),
            state["entry"].orEmpty(), state["message"].orEmpty(),
        )
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
        val word = submitted.trim().uppercase(Locale.ROOT)
        val error = wordError(puzzle, word)
        if (error != null) return state.copy(message = error)
        if (word in state.found) return state.copy(message = "You already found $word. No extra points.")
        val next = state.copy(found = state.found + word, entry = "")
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
        if (puzzle.letters.length != 7 || puzzle.letters.toSet().size != 7 || puzzle.letters.any { it !in 'A'..'Z' }) add("Exactly seven distinct uppercase letters required")
        if (puzzle.center !in puzzle.letters) add("Center must be one of the seven letters")
        if (puzzle.dictionaryVersion.isBlank()) add("Missing pinned dictionary version")
        if (puzzle.scoringVersion !in setOf(BeePuzzle.LEGACY_SCORING, BeePuzzle.WORD_COUNT_SCORING)) add("Unsupported scoring version")
        if (puzzle.goalWords.isEmpty()) add("Missing goal words")
        if (!puzzle.validWords.containsAll(puzzle.goalWords)) add("Every goal word must be dictionary accepted")
        if (puzzle.goalWords.none { pangram(puzzle, it) }) add("At least one goal pangram required")
        puzzle.validWords.forEach { word -> if (wordError(puzzle, word) != null) add("Invalid accepted word: $word") }
        if (puzzle.target <= 0 || puzzle.target > puzzle.maximumGoalScore) add("Target must be attainable")
        if (puzzle.wordTarget <= 0 || puzzle.wordTarget > puzzle.validWords.size) add("Word-count target must be attainable")
    }
}
