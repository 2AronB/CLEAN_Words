package dev.clean.words.games.connections

import dev.clean.words.core.PlayStatus
import org.junit.Assert.*
import org.junit.Test

class ConnectionsRulesTest {
    private val puzzle = ConnectionsContent.puzzles.first()
    private fun choose(vararg tiles: Int, state: Map<String, String> = emptyMap()): Map<String, String> =
        tiles.fold(ConnectionsRules.clear(state)) { current, tile -> ConnectionsRules.select(current, tile) }

    @Test fun allTenBoardsHaveDistinctIdentitiesValidContentAndProvenance() {
        assertEquals(10, ConnectionsContent.puzzles.size)
        assertEquals(10, ConnectionsContent.puzzles.map { it.id }.distinct().size)
        assertEquals(10, ConnectionsContent.puzzles.map { it.data }.distinct().size)
        ConnectionsContent.puzzles.forEach { assertEquals(it.id, emptyList<String>(), ConnectionsRules.validate(it)) }
    }
    @Test fun eachOfTenBoardsCanBeCompletedByItsFourGroups() {
        ConnectionsContent.puzzles.forEach { board ->
            var state = emptyMap<String, String>()
            for (i in 0..3) state = ConnectionsRules.submit(board, choose(i * 4, i * 4 + 1, i * 4 + 2, i * 4 + 3, state = state))
            assertEquals(PlayStatus.COMPLETED, ConnectionsRules.result(state).status)
            assertEquals(4, ConnectionsRules.result(state).score)
            assertEquals(0, ConnectionsRules.mistakes(state).size)
        }
    }
    @Test fun incompleteGroupDoesNotSpendMistake() {
        val state = ConnectionsRules.submit(puzzle, choose(0, 1, 2))
        assertEquals(0, ConnectionsRules.mistakes(state).size)
        assertTrue(state["message"]!!.contains("exactly four"))
    }
    @Test fun fifthSelectionDoesNotEnterGroupAndSelectedTileCanBeDeselected() {
        val full = choose(0, 1, 2, 3)
        assertEquals(setOf(0, 1, 2, 3), ConnectionsRules.selection(ConnectionsRules.select(full, 4)))
        assertEquals(setOf(0, 1, 2), ConnectionsRules.selection(ConnectionsRules.select(full, 3)))
        assertEquals(full, ConnectionsRules.select(full, 16))
    }
    @Test fun repeatedWrongSetInAnyOrderDoesNotSpendAnotherMistake() {
        val first = ConnectionsRules.submit(puzzle, choose(0, 1, 2, 4))
        assertTrue(first["message"]!!.contains("One away"))
        val again = ConnectionsRules.submit(puzzle, choose(4, 2, 1, 0, state = first))
        assertEquals(1, ConnectionsRules.mistakes(again).size)
        assertTrue(again["message"]!!.contains("already tried"))
    }
    @Test fun fourthDistinctMistakeEndsGameAndTerminalStateCannotBeEdited() {
        var state = emptyMap<String, String>()
        for (last in 4..7) state = ConnectionsRules.submit(puzzle, choose(0, 1, 2, last, state = state))
        assertEquals(PlayStatus.FAILED, ConnectionsRules.result(state).status)
        assertEquals(state, ConnectionsRules.select(state, 0))
        assertEquals(state, ConnectionsRules.clear(state))
        assertEquals(state, ConnectionsRules.submit(puzzle, state))
    }
    @Test fun restoreMapRetainsGroupsMistakesAndSelection() {
        var state = ConnectionsRules.submit(puzzle, choose(0, 1, 2, 3))
        state = ConnectionsRules.submit(puzzle, choose(4, 5, 6, 8, state = state))
        state = choose(8, 9, state = state)
        val restored = state.entries.associate { it.key to it.value }
        assertEquals(setOf(0), ConnectionsRules.solved(restored))
        assertEquals(setOf(8, 9), ConnectionsRules.selection(restored))
        assertEquals(1, ConnectionsRules.mistakes(restored).size)
        assertEquals(ConnectionsRules.result(state), ConnectionsRules.result(restored))
        assertEquals(restored, ConnectionsRules.select(restored, 0))
    }
    @Test fun completedStateCannotBeEdited() {
        val state = mapOf("solved" to "0,1,2,3", "selected" to "")
        assertEquals(PlayStatus.COMPLETED, ConnectionsRules.result(state).status)
        assertEquals(state, ConnectionsRules.select(state, 0))
        assertEquals(state, ConnectionsRules.submit(puzzle, state))
    }
    @Test fun validationFindsDuplicateAndMissingExplanation() {
        val broken = puzzle.copy(data = puzzle.data + mapOf("group.1.words" to "PENNE|A|B|C", "group.0.explanation" to ""))
        assertTrue(ConnectionsRules.validate(broken).any { it.contains("distinct") })
        assertTrue(ConnectionsRules.validate(broken).any { it.contains("explanation") })
    }
}
