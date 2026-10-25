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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
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
import dev.clean.words.ui.GameActions

object ConnectionsGame : GameModule {
    override val id = "connections"
    override val title = "Connektions"
    override val description = "Find four groups of four related words. Four distinct mistakes end the puzzle."
    override val puzzles = ConnectionsContent.puzzles
    override fun result(puzzle: PuzzleRecord, state: Map<String, String>): GameResult = ConnectionsRules.result(state)
    override fun validate(puzzle: PuzzleRecord) = ConnectionsRules.validate(puzzle)

    @Composable
    override fun Play(puzzle: PuzzleRecord, state: Map<String, String>, enabled: Boolean, onChange: (Map<String, String>) -> Unit) {
        var live by remember(puzzle.id) { mutableStateOf(state) }
        fun dispatch(action: (Map<String, String>) -> Map<String, String>) {
            val next = action(live); live = next; onChange(next)
        }
        val groups = ConnectionsRules.groups(puzzle)
        val result = result(puzzle, live)
        val active = enabled && result.status == PlayStatus.IN_PROGRESS
        val solved = ConnectionsRules.solved(live)
        val selected = ConnectionsRules.selection(live)
        val remaining = (0..15).shuffled(Random(puzzle.id.hashCode())).filter { it / 4 !in solved }
        Column(Modifier.animateContentSize(tween(260)), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(description)
            Text("Groups: ${solved.size}/4  •  Mistakes remaining: ${(4 - ConnectionsRules.mistakes(live).size).coerceAtLeast(0)}")
            groups.forEachIndexed { i, group ->
                if (i in solved) GroupCard("✓ ${group.label}", group)
            }
            if (result.status != PlayStatus.FAILED) {
                remaining.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { tile ->
                            val color by animateColorAsState(if (tile in selected) Color(0xFFC6ABF5) else Color(0xFFFFE9B6), tween(170), label = "selected-word")
                            OutlinedButton(onClick = { if (active) dispatch { ConnectionsRules.select(it, tile) } }, enabled = active,
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = color, contentColor = Color(0xFF352C43)),
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
                GameActions(onClear = { dispatch(ConnectionsRules::clear) },
                    onEnter = { dispatch { ConnectionsRules.submit(puzzle, it) } },
                    onDelete = { dispatch(ConnectionsRules::delete) }, enabled = active,
                    clearEnabled = selected.isNotEmpty(), enterEnabled = selected.size == 4,
                    deleteEnabled = selected.isNotEmpty())
            }
            live["message"]?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            if (result.status == PlayStatus.FAILED) groups.forEachIndexed { i, group ->
                if (i !in solved) GroupCard("Revealed: ${group.label}", group)
            }
            if (result.status != PlayStatus.IN_PROGRESS) Text(result.summary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GroupCard(heading: String, group: ConnectionsGroup) {
    Card(modifier = Modifier.fillMaxWidth().animateContentSize(tween(240)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6DA), contentColor = Color(0xFF253B34))) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(heading, fontWeight = FontWeight.Bold)
            Text(group.words.joinToString(" · "))
            Text(group.explanation, style = MaterialTheme.typography.bodySmall)
        }
    }
}
