package dev.clean.words.games.connections

import dev.clean.words.core.GameResult
import dev.clean.words.core.PlayStatus
import dev.clean.words.core.PuzzleRecord

data class ConnectionsGroup(val label: String, val words: List<String>, val explanation: String)

object ConnectionsRules {
    fun groups(puzzle: PuzzleRecord): List<ConnectionsGroup> = (0..3).map { i ->
        ConnectionsGroup(puzzle.data["group.$i.label"].orEmpty(),
            puzzle.data["group.$i.words"].orEmpty().split('|'),
            puzzle.data["group.$i.explanation"].orEmpty())
    }

    fun validate(puzzle: PuzzleRecord): List<String> = buildList {
        if (puzzle.id.isBlank()) add("Missing puzzle identity")
        val groups = groups(puzzle)
        groups.forEachIndexed { i, group ->
            if (group.label.isBlank() || group.explanation.isBlank()) add("Group $i needs a label and explanation")
            if (group.words.size != 4 || group.words.any { it.isBlank() }) add("Group $i needs four words")
            if (group.words.any { it != it.trim().uppercase() }) add("Group $i words must be normalized uppercase")
        }
        if (groups.flatMap { it.words }.distinct().size != 16) add("A board needs sixteen distinct tiles")
        if (puzzle.data["origin"].isNullOrBlank()) add("Missing provenance")
    }

    private fun indices(value: String?): List<Int> = value.orEmpty().split(',').mapNotNull { it.toIntOrNull() }
    fun solved(state: Map<String, String>): Set<Int> = indices(state["solved"]).filter { it in 0..3 }.toSet()
    fun selection(state: Map<String, String>): Set<Int> = indices(state["selected"]).filter { it in 0..15 && it / 4 !in solved(state) }.toSet().take(4).toSet()
    fun mistakes(state: Map<String, String>): Set<String> = state["mistakes"].orEmpty().split(';').filter { key ->
        val values = key.split('-').mapNotNull { it.toIntOrNull() }
        values.size == 4 && values.distinct().size == 4 && values.all { it in 0..15 } && values.sorted().joinToString("-") == key
    }.toSet()

    fun result(state: Map<String, String>): GameResult {
        val count = solved(state).size
        return when {
            count == 4 -> GameResult(PlayStatus.COMPLETED, "All four groups found", count, 4)
            mistakes(state).size >= 4 -> GameResult(PlayStatus.FAILED, "$count of 4 groups found; four mistakes", count, 4)
            else -> GameResult(PlayStatus.IN_PROGRESS, "$count of 4 groups found", count, 4)
        }
    }

    fun select(state: Map<String, String>, tile: Int): Map<String, String> {
        if (result(state).status != PlayStatus.IN_PROGRESS || tile !in 0..15 || tile / 4 in solved(state)) return state
        val selected = selection(state).toMutableSet()
        if (!selected.remove(tile)) {
            if (selected.size >= 4) return state + ("message" to "Select at most four tiles. Deselect one to change your group.")
            selected.add(tile)
        }
        return state + mapOf("selected" to selected.joinToString(","), "message" to "")
    }

    fun delete(state: Map<String, String>): Map<String, String> {
        if (result(state).status != PlayStatus.IN_PROGRESS) return state
        val selected = selection(state).toList()
        return state + mapOf("selected" to selected.dropLast(1).joinToString(","), "message" to "")
    }

    fun clear(state: Map<String, String>): Map<String, String> =
        if (result(state).status != PlayStatus.IN_PROGRESS) state else state + mapOf("selected" to "", "message" to "")

    fun submit(puzzle: PuzzleRecord, state: Map<String, String>): Map<String, String> {
        if (result(state).status != PlayStatus.IN_PROGRESS) return state
        val selected = selection(state)
        if (selected.size != 4) return state + ("message" to "Select exactly four tiles before submitting.")
        val group = (0..3).firstOrNull { i -> selected == (i * 4 until i * 4 + 4).toSet() }
        if (group != null) {
            return state + mapOf("solved" to (solved(state) + group).sorted().joinToString(","),
                "selected" to "", "message" to "Found: ${groups(puzzle)[group].label}")
        }
        val key = selected.sorted().joinToString("-")
        if (key in mistakes(state)) return state + ("message" to "You already tried this group. No additional mistake counted.")
        val wrong = mistakes(state) + key
        val oneAway = selected.groupingBy { it / 4 }.eachCount().values.any { it == 3 }
        return state + mapOf("mistakes" to wrong.joinToString(";"), "message" to
            if (wrong.size == 4) "Four mistakes. The remaining groups are revealed below."
            else if (oneAway) "One away: three of those tiles belong together." else "Not a group. Try a different connection.")
    }
}
