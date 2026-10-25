package dev.clean.words.games.connections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.clean.words.core.GameModule
import dev.clean.words.core.GameResult
import dev.clean.words.core.PlayStatus
import dev.clean.words.core.PuzzleRecord
import kotlin.random.Random

object ConnectionsGame : GameModule {
    override val id = "connections"
    override val title = "Connektions"
    override val description = "Find four groups of four related words. Four distinct mistakes end the puzzle."
    override val puzzles = ConnectionsContent.puzzles
    override fun result(puzzle: PuzzleRecord, state: Map<String, String>): GameResult = ConnectionsRules.result(state)
    override fun validate(puzzle: PuzzleRecord) = ConnectionsRules.validate(puzzle)

    @Composable
    override fun Play(puzzle: PuzzleRecord, state: Map<String, String>, enabled: Boolean, onChange: (Map<String, String>) -> Unit) {
        val groups = ConnectionsRules.groups(puzzle)
        val result = result(puzzle, state)
        val active = enabled && result.status == PlayStatus.IN_PROGRESS
        val solved = ConnectionsRules.solved(state)
        val selected = ConnectionsRules.selection(state)
        val remaining = (0..15).shuffled(Random(puzzle.id.hashCode())).filter { it / 4 !in solved }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(description)
            Text("Groups: ${solved.size}/4  •  Mistakes remaining: ${(4 - ConnectionsRules.mistakes(state).size).coerceAtLeast(0)}")
            groups.forEachIndexed { i, group ->
                if (i in solved) GroupCard("✓ ${group.label}", group)
            }
            if (result.status != PlayStatus.FAILED) {
                remaining.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { tile ->
                            OutlinedButton(onClick = { if (active) onChange(ConnectionsRules.select(state, tile)) }, enabled = active,
                                contentPadding = PaddingValues(horizontal = 3.dp, vertical = 10.dp),
                                modifier = Modifier.weight(1f).heightIn(min = 72.dp)) {
                                Text((if (tile in selected) "✓ " else "") + groups[tile / 4].words[tile % 4],
                                    fontSize = 12.sp, fontWeight = if (tile in selected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
            if (result.status == PlayStatus.IN_PROGRESS) {
                Text("${selected.size} of 4 selected")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { if (active) onChange(ConnectionsRules.submit(puzzle, state)) }, enabled = active && selected.size == 4) { Text("Submit group") }
                    OutlinedButton(onClick = { if (active) onChange(ConnectionsRules.clear(state)) }, enabled = active && selected.isNotEmpty()) { Text("Clear") }
                }
            }
            state["message"]?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            if (result.status == PlayStatus.FAILED) groups.forEachIndexed { i, group ->
                if (i !in solved) GroupCard("Revealed: ${group.label}", group)
            }
            if (result.status != PlayStatus.IN_PROGRESS) Text(result.summary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GroupCard(heading: String, group: ConnectionsGroup) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(heading, fontWeight = FontWeight.Bold)
            Text(group.words.joinToString(" · "))
            Text(group.explanation, style = MaterialTheme.typography.bodySmall)
        }
    }
}
