package dev.clean.words.games.crossword

import dev.clean.words.core.PlayStatus
import java.util.Locale

/** Cursor/edit operations never inspect whether an individual word is correct. */
object CrosswordInput {
    fun orderedEntries(board: CrosswordBoard): List<CrosswordEntry> = board.entries()
        .sortedWith(compareBy<CrosswordEntry> { it.direction.ordinal }.thenBy { it.number })

    fun selected(board: CrosswordBoard, state: Map<String, String>): Int = state["selected"]
        ?.toIntOrNull()?.takeIf { it in board.grid.indices && board.grid[it] != '#' }
        ?: board.grid.indexOfFirst { it != '#' }

    fun direction(state: Map<String, String>): CrosswordDirection = CrosswordDirection.entries
        .firstOrNull { it.name == state["direction"] } ?: CrosswordDirection.ACROSS

    fun current(board: CrosswordBoard, state: Map<String, String>): CrosswordEntry? {
        val at = selected(board, state)
        return orderedEntries(board).firstOrNull { at in it.cells && it.direction == direction(state) }
            ?: orderedEntries(board).firstOrNull { at in it.cells }
    }

    fun select(board: CrosswordBoard, state: Map<String, String>, at: Int, wanted: CrosswordDirection): Map<String,String> {
        if (at !in board.grid.indices || board.grid[at] == '#') return state
        val entry = orderedEntries(board).firstOrNull { at in it.cells && it.direction == wanted }
            ?: orderedEntries(board).firstOrNull { at in it.cells } ?: return state
        return state + mapOf("selected" to at.toString(), "direction" to entry.direction.name)
    }

    fun nextClue(board: CrosswordBoard, state: Map<String, String>): Map<String,String> {
        val entries = orderedEntries(board)
        val current = current(board, state) ?: return state
        val start = entries.indexOf(current)
        val cells = CrosswordRules.cells(board, state)
        val next = (1..entries.size).map { entries[(start + it) % entries.size] }
            .firstOrNull { entry -> entry.cells.any { cells[it] == '.' } }
            ?: entries[(start + 1) % entries.size]
        val at = next.cells.firstOrNull { cells[it] == '.' } ?: next.cells.first()
        return select(board, state, at, next.direction)
    }

    /** One event may contain a whole IME commit/paste; every letter is applied in order. */
    fun type(board: CrosswordBoard, state: Map<String,String>, input: String): Map<String,String> {
        var next = state
        input.uppercase(Locale.ROOT).filter { it in 'A'..'Z' }.forEach { letter ->
            if (CrosswordRules.result(board, next).status == PlayStatus.COMPLETED) return next
            val entry = current(board, next) ?: return next
            val at = selected(board, next)
            next = CrosswordRules.enter(board, next, at, letter.toString())
            val cells = CrosswordRules.cells(board, next)
            next = if (entry.cells.all { cells[it] != '.' }) {
                nextClue(board, next)
            } else {
                val index = entry.cells.indexOf(at)
                val remaining = entry.cells.drop(index + 1) + entry.cells.take(index + 1)
                val blank = remaining.firstOrNull { cells[it] == '.' } ?: at
                select(board, next, blank, entry.direction)
            }
        }
        return next
    }

    fun delete(board: CrosswordBoard, state: Map<String,String>): Map<String,String> {
        if (CrosswordRules.result(board, state).status == PlayStatus.COMPLETED) return state
        val entry = current(board, state) ?: return state
        val at = selected(board, state)
        val atInEntry = entry.cells.indexOf(at)
        val previousEntry = if (atInEntry > 0) entry else {
            val entries = orderedEntries(board)
            entries[(entries.indexOf(entry) - 1 + entries.size) % entries.size]
        }
        val previous = if (atInEntry > 0) entry.cells[atInEntry - 1] else previousEntry.cells.last()
        val cells = CrosswordRules.cells(board, state)
        val cleared = if (cells[at] == '.') previous else at
        val next = CrosswordRules.enter(board, state, cleared, "")
        return select(board, next, previous, previousEntry.direction)
    }

    fun clearClue(board: CrosswordBoard, state: Map<String,String>): Map<String,String> {
        if (CrosswordRules.result(board, state).status == PlayStatus.COMPLETED) return state
        val entry = current(board, state) ?: return state
        var next = state
        entry.cells.forEach { next = CrosswordRules.enter(board, next, it, "") }
        return select(board, next, entry.cells.first(), entry.direction)
    }

    data class BufferEdit(val deletes: Int, val inserted: String)

    /** A local append-only keyboard stream keeps IME bursts independent of parent recomposition. */
    fun bufferEdit(previous: String, incoming: String): BufferEdit {
        val before = previous.uppercase(Locale.ROOT).filter { it in 'A'..'Z' }
        val after = incoming.uppercase(Locale.ROOT).filter { it in 'A'..'Z' }
        val shared = before.zip(after).takeWhile { it.first == it.second }.size
        val deletes = (before.length - shared).takeUnless { before.isEmpty() && incoming.isEmpty() } ?: 1
        return BufferEdit(deletes, after.drop(shared))
    }
}
