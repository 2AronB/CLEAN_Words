package dev.clean.words.games.crossword

import dev.clean.words.core.GameResult
import dev.clean.words.core.PlayStatus
import dev.clean.words.core.PuzzleRecord

enum class CrosswordDirection { ACROSS, DOWN }

data class CrosswordEntry(
    val number: Int,
    val direction: CrosswordDirection,
    val cells: List<Int>,
    val answer: String,
    val clue: String,
)

data class CrosswordBoard(val grid: String, val clues: Map<String, String>, val width: Int = 5, val height: Int = 5) {
    fun entries(): List<CrosswordEntry> {
        if (width !in setOf(5, 7) || height !in setOf(5, 7) || grid.length != width * height) return emptyList()
        val starts = mutableListOf<Triple<Int, CrosswordDirection, List<Int>>>()
        for (index in grid.indices) {
            if (grid[index] == '#') continue
            val row = index / width
            val col = index % width
            if (col == 0 || grid[index - 1] == '#') {
                val cells = (col until width).map { row * width + it }.takeWhile { grid[it] != '#' }
                if (cells.size > 1) starts += Triple(index, CrosswordDirection.ACROSS, cells)
            }
            if (row == 0 || grid[index - width] == '#') {
                val cells = (row until height).map { it * width + col }.takeWhile { grid[it] != '#' }
                if (cells.size > 1) starts += Triple(index, CrosswordDirection.DOWN, cells)
            }
        }
        val numbering = starts.map { it.first }.distinct().sorted().withIndex()
            .associate { it.value to it.index + 1 }
        return starts.map { (start, direction, cells) ->
            val answer = cells.map { grid[it] }.joinToString("")
            CrosswordEntry(numbering.getValue(start), direction, cells, answer, clues[answer].orEmpty())
        }
    }
}

/** Pure game rules. State is a complete, app-owned snapshot; no file or clock access. */
object CrosswordRules {
    fun board(puzzle: PuzzleRecord): CrosswordBoard? {
        val grid = puzzle.data["grid"].orEmpty().replace("\n", "").replace("\r", "")
        val hasDimensions = "width" in puzzle.data || "height" in puzzle.data
        val width = if (hasDimensions) puzzle.data["width"]?.toIntOrNull() ?: return null else 5
        val height = if (hasDimensions) puzzle.data["height"]?.toIntOrNull() ?: return null else 5
        if (width !in setOf(5, 7) || height !in setOf(5, 7)) return null
        if (grid.length != width * height || grid.any { it != '#' && it !in 'A'..'Z' }) return null
        val clues = puzzle.data["clues"].orEmpty().lineSequence().mapNotNull { line ->
            val split = line.indexOf('|')
            if (split < 1) null else line.substring(0, split) to line.substring(split + 1)
        }.toMap()
        return CrosswordBoard(grid, clues, width, height)
    }

    fun validate(puzzle: PuzzleRecord): List<String> {
        val board = board(puzzle) ?: return listOf("Grid must match supported dimensions with uppercase letters or blocks")
        val errors = mutableListOf<String>()
        val entries = board.entries()
        if (entries.isEmpty()) errors += "Grid has no entries"
        if (entries.any { it.cells.size < 3 }) errors += "Entries must have at least three letters"
        if (entries.none { it.direction == CrosswordDirection.ACROSS } ||
            entries.none { it.direction == CrosswordDirection.DOWN }) errors += "Both directions are required"
        val used = entries.flatMap { it.cells }.toSet()
        val open = board.grid.indices.filter { board.grid[it] != '#' }.toSet()
        if (used != open) errors += "Every open cell must belong to a word"
        if (entries.any { it.clue.isBlank() }) errors += "Every answer needs a clue"
        if (entries.map { it.answer }.distinct().size != entries.size) errors += "Repeated answers in grid"
        if (board.clues.keys != entries.map { it.answer }.toSet()) errors += "Clue/answer set mismatch"
        if (open.isNotEmpty()) {
            val reached = mutableSetOf(open.first())
            val queue = ArrayDeque<Int>().apply { add(open.first()) }
            while (queue.isNotEmpty()) {
                val index = queue.removeFirst()
                val width = board.width
                val height = board.height
                val neighbors = listOfNotNull(
                    (index - width).takeIf { index / width > 0 },
                    (index + width).takeIf { index / width < height - 1 },
                    (index - 1).takeIf { index % width > 0 },
                    (index + 1).takeIf { index % width < width - 1 },
                )
                neighbors.filter { it in open && reached.add(it) }.forEach(queue::addLast)
            }
            if (reached != open) errors += "Grid must be connected"
        }
        return errors
    }

    fun cells(board: CrosswordBoard, state: Map<String, String>): String {
        val saved = state["cells"].orEmpty()
        return board.grid.mapIndexed { index, answer ->
            if (answer == '#') '#' else saved.getOrNull(index)?.takeIf { it in 'A'..'Z' } ?: '.'
        }.joinToString("")
    }

    fun enter(board: CrosswordBoard, state: Map<String, String>, index: Int, input: String): Map<String, String> {
        if (index !in board.grid.indices || board.grid[index] == '#' || result(board, state).status == PlayStatus.COMPLETED) return state
        val normalized = input.uppercase(java.util.Locale.ROOT)
        if (normalized.isNotEmpty() && (normalized.length != 1 || normalized[0] !in 'A'..'Z')) return state
        val updated = cells(board, state).toCharArray()
        updated[index] = normalized.firstOrNull() ?: '.'
        val checked = checked(state) - index
        return state + mapOf("cells" to updated.concatToString(), "checked" to checked.sorted().joinToString(","))
    }

    fun checked(state: Map<String, String>): Set<Int> = state["checked"].orEmpty().split(',')
        .mapNotNull { it.toIntOrNull() }.filter { it in 0..48 }.toSet()

    fun check(board: CrosswordBoard, state: Map<String, String>): Map<String, String> {
        if (result(board, state).status == PlayStatus.COMPLETED) return state
        val cells = cells(board, state)
        val wrong = board.grid.indices.filter { board.grid[it] != '#' && cells[it] != '.' && cells[it] != board.grid[it] }
        return state + mapOf("checked" to wrong.joinToString(","), "assisted" to "true", "checkedOnce" to "true")
    }

    fun result(board: CrosswordBoard, state: Map<String, String>): GameResult {
        val cells = cells(board, state)
        val total = board.grid.count { it != '#' }
        val filled = board.grid.indices.count { board.grid[it] != '#' && cells[it] != '.' }
        val solved = total > 0 && cells == board.grid
        return GameResult(
            status = if (solved) PlayStatus.COMPLETED else PlayStatus.IN_PROGRESS,
            summary = if (solved) "Crossword complete" else "$filled of $total letters entered",
            score = if (solved) total else null,
            maxScore = total,
            assisted = state["assisted"] == "true",
        )
    }

    fun nextCell(entry: CrosswordEntry, index: Int): Int {
        val at = entry.cells.indexOf(index)
        return entry.cells.getOrElse(at + 1) { index }
    }
}
