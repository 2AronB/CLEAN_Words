package dev.clean.words.games.strands

import dev.clean.words.core.GameResult
import dev.clean.words.core.PlayStatus
import dev.clean.words.core.PuzzleRecord
import kotlin.math.abs

data class StrandAnswer(val word: String, val path: List<Int>)

data class StrandsPuzzle(
    val rows: Int,
    val columns: Int,
    val letters: String,
    val clue: String,
    val spangram: String,
    val answers: List<StrandAnswer>,
) {
    fun snapshot(id: String): PuzzleRecord = PuzzleRecord(id, title = clue, data = mapOf(
        "rows" to rows.toString(), "columns" to columns.toString(),
        "letters" to letters, "clue" to clue, "spangram" to spangram,
        "answers" to answers.joinToString("|") { "${it.word}:${it.path.joinToString(",")}" },
    ))

    companion object {
        fun from(record: PuzzleRecord): StrandsPuzzle = StrandsPuzzle(
            rows = record.data["rows"]?.toIntOrNull() ?: 0,
            columns = record.data["columns"]?.toIntOrNull() ?: 0,
            letters = record.data["letters"].orEmpty(),
            clue = record.data["clue"].orEmpty(),
            spangram = record.data["spangram"].orEmpty(),
            answers = decodePaths(record.data["answers"].orEmpty()),
        )
    }
}

data class StrandsState(
    val found: List<StrandAnswer> = emptyList(),
    val selected: List<Int> = emptyList(),
    val message: String = "",
) {
    fun toMap(): Map<String, String> = mapOf(
        "found" to found.joinToString("|") { "${it.word}:${it.path.joinToString(",")}" },
        "selected" to selected.joinToString(","), "message" to message,
    )

    companion object {
        fun from(state: Map<String, String>) = StrandsState(
            found = decodePaths(state["found"].orEmpty()),
            selected = decodeIndices(state["selected"].orEmpty()),
            message = state["message"].orEmpty(),
        )
    }
}

private fun decodeIndices(text: String): List<Int> {
    if (text.isBlank()) return emptyList()
    val fields = text.split(',')
    return fields.map { it.toIntOrNull() ?: return emptyList() }
}

private fun decodePaths(text: String): List<StrandAnswer> =
    if (text.isBlank()) emptyList() else text.split('|').mapNotNull { field ->
        val parts = field.split(':', limit = 2)
        if (parts.size != 2 || parts[0].isBlank()) null
        else decodeIndices(parts[1]).takeIf { it.isNotEmpty() }?.let { StrandAnswer(parts[0], it) }
    }

/** Pure rules. Board coordinates are flattened row-major positions. */
object StrandsRules {
    fun adjacent(a: Int, b: Int, columns: Int): Boolean {
        if (columns <= 0 || a == b || a < 0 || b < 0) return false
        return abs(a / columns - b / columns) <= 1 && abs(a % columns - b % columns) <= 1
    }

    fun legalPath(puzzle: StrandsPuzzle, path: List<Int>): Boolean =
        puzzle.rows > 0 && puzzle.columns > 0 && path.isNotEmpty() && path.all { it in puzzle.letters.indices } &&
            path.toSet().size == path.size && path.zipWithNext().all { adjacent(it.first, it.second, puzzle.columns) }

    fun spelling(puzzle: StrandsPuzzle, path: List<Int>): String =
        if (path.all { it in puzzle.letters.indices }) path.map { puzzle.letters[it] }.joinToString("") else ""

    fun spans(puzzle: StrandsPuzzle, path: List<Int>): Boolean {
        if (!legalPath(puzzle, path)) return false
        return (path.any { it / puzzle.columns == 0 } && path.any { it / puzzle.columns == puzzle.rows - 1 }) ||
            (path.any { it % puzzle.columns == 0 } && path.any { it % puzzle.columns == puzzle.columns - 1 })
    }

    fun validFound(puzzle: StrandsPuzzle, state: StrandsState): Boolean {
        val indices = state.found.flatMap { it.path }
        return state.found.map { it.word }.distinct().size == state.found.size &&
            indices.distinct().size == indices.size && state.found.all { answer ->
                puzzle.answers.any { it.word == answer.word } && legalPath(puzzle, answer.path) &&
                    spelling(puzzle, answer.path) == answer.word &&
                    (answer.word != puzzle.spangram || spans(puzzle, answer.path))
            }
    }

    fun tap(puzzle: StrandsPuzzle, state: StrandsState, index: Int): StrandsState {
        if (index !in puzzle.letters.indices || result(puzzle, state).status == PlayStatus.COMPLETED) return state
        if (state.found.any { index in it.path }) return state.copy(message = "That letter is in a found word. Undo the last word to change its route.")
        val previous = state.selected.indexOf(index)
        if (previous >= 0) {
            val keep = if (previous == state.selected.lastIndex) previous else previous + 1
            return state.copy(selected = state.selected.take(keep), message = "")
        }
        if (state.selected.isNotEmpty() && !adjacent(state.selected.last(), index, puzzle.columns))
            return state.copy(message = "Choose a neighboring letter, including diagonals.")
        return state.copy(selected = state.selected + index, message = "")
    }

    fun submit(puzzle: StrandsPuzzle, state: StrandsState): StrandsState {
        if (result(puzzle, state).status == PlayStatus.COMPLETED) return state
        if (!validFound(puzzle, state)) return state.copy(message = "Saved paths are not valid for this puzzle. Your data has not been changed.")
        if (!legalPath(puzzle, state.selected)) return state.copy(message = "Choose a connected path without reusing a letter.")
        val word = spelling(puzzle, state.selected)
        if (state.found.any { it.word == word }) return state.copy(message = "You already found $word.")
        if (state.found.any { found -> found.path.any { it in state.selected } })
            return state.copy(message = "Found words cannot share letters. Clear this path and try another route.")
        if (puzzle.answers.none { it.word == word }) return state.copy(message = "$word is not one of this puzzle's theme words. There is no penalty.")
        if (word == puzzle.spangram && !spans(puzzle, state.selected))
            return state.copy(message = "The spanning answer must connect opposite edges. Try another route.")
        val next = state.copy(found = state.found + StrandAnswer(word, state.selected), selected = emptyList())
        return next.copy(message = if (result(puzzle, next).status == PlayStatus.COMPLETED) "Every letter connected. Puzzle complete!"
            else if (word == puzzle.spangram) "Spanning answer found: $word" else "Theme word found: $word")
    }

    fun undo(state: StrandsState): StrandsState = if (state.found.isEmpty()) state.copy(selected = emptyList(), message = "")
        else state.copy(found = state.found.dropLast(1), selected = emptyList(), message = "Last word returned to the board. Try a different route.")

    fun result(puzzle: StrandsPuzzle, state: StrandsState): GameResult {
        val valid = validFound(puzzle, state)
        val covered = state.found.flatMap { it.path }.toSet()
        val complete = valid && puzzle.answers.isNotEmpty() && state.found.size == puzzle.answers.size &&
            covered.size == puzzle.letters.length
        return GameResult(
            status = if (complete) PlayStatus.COMPLETED else PlayStatus.IN_PROGRESS,
            summary = if (complete) "All ${puzzle.answers.size} theme words found" else "${if (valid) state.found.size else 0} of ${puzzle.answers.size} theme words",
            score = if (valid) state.found.size else 0, maxScore = puzzle.answers.size,
        )
    }

    fun validate(puzzle: StrandsPuzzle): List<String> = buildList {
        if (puzzle.rows !in 3..10 || puzzle.columns !in 3..10) add("Board dimensions must be between 3 and 10")
        if (puzzle.rows * puzzle.columns != puzzle.letters.length) add("Board length differs from dimensions")
        if (!puzzle.letters.all { it in 'A'..'Z' }) add("Board requires uppercase English letters")
        if (puzzle.clue.isBlank()) add("Missing theme clue")
        if (puzzle.answers.size < 3) add("At least three themed answers required")
        if (puzzle.answers.map { it.word }.distinct().size != puzzle.answers.size) add("Duplicate answer")
        if (puzzle.answers.any { it.word.length < 4 || !it.word.all { letter -> letter in 'A'..'Z' } }) add("Answers must be uppercase words of four or more letters")
        puzzle.answers.forEach { answer ->
            if (!legalPath(puzzle, answer.path)) add("Illegal path: ${answer.word}")
            if (spelling(puzzle, answer.path) != answer.word) add("Path does not spell ${answer.word}")
        }
        if (puzzle.answers.count { it.word == puzzle.spangram } != 1) add("Exactly one spanning answer required")
        else if (!spans(puzzle, puzzle.answers.first { it.word == puzzle.spangram }.path)) add("Spanning answer does not touch opposite edges")
        val all = puzzle.answers.flatMap { it.path }
        if (all.distinct().size != all.size || all.toSet() != puzzle.letters.indices.toSet()) add("Answer paths must partition every board cell")
    }
}
