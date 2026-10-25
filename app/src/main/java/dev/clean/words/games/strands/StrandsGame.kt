package dev.clean.words.games.strands

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.clean.words.core.GameModule
import dev.clean.words.core.GameResult
import dev.clean.words.core.PlayStatus
import dev.clean.words.core.PuzzleRecord

object StrandsGame : GameModule {
    override val id = "strands"
    override val title = "Strand-ish"
    override val description = "Connect neighboring letters into themed words."
    override val puzzles = StrandsContent.puzzles

    override fun result(puzzle: PuzzleRecord, state: Map<String, String>): GameResult =
        StrandsRules.result(StrandsPuzzle.from(puzzle), StrandsState.from(state))

    override fun validate(puzzle: PuzzleRecord): List<String> = StrandsRules.validate(StrandsPuzzle.from(puzzle))

    @Composable
    override fun Play(puzzle: PuzzleRecord, state: Map<String, String>, enabled: Boolean, onChange: (Map<String, String>) -> Unit) {
        val board = StrandsPuzzle.from(puzzle)
        val progress = StrandsState.from(state)
        val outcome = StrandsRules.result(board, progress)
        val canPlay = enabled && outcome.status != PlayStatus.COMPLETED
        val contentErrors = StrandsRules.validate(board)
        if (contentErrors.isNotEmpty()) {
            Text("This saved board cannot be played: ${contentErrors.joinToString()}. Your progress has not been erased.")
            return
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(board.clue, style = MaterialTheme.typography.titleLarge)
            Text("Find ${board.answers.size} theme words. One spanning answer joins opposite edges.", style = MaterialTheme.typography.bodyMedium)
            Text("Tap neighboring letters, including diagonals, then submit. Tap the last selected letter to step back. Words cannot share letters.", style = MaterialTheme.typography.bodySmall)
            Text(outcome.summary, fontWeight = FontWeight.SemiBold)
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(board.rows) { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(board.columns) { column ->
                            val cell = row * board.columns + column
                            val found = progress.found.firstOrNull { cell in it.path }
                            val step = progress.selected.indexOf(cell)
                            val isSpan = found?.word == board.spangram
                            val colors = MaterialTheme.colorScheme
                            val container = when {
                                isSpan -> colors.tertiaryContainer
                                found != null -> colors.primaryContainer
                                step >= 0 -> colors.secondaryContainer
                                else -> colors.surfaceContainerHigh
                            }
                            val label = when {
                                found != null -> "${board.letters[cell]} ✓"
                                step >= 0 -> "${board.letters[cell]} ${step + 1}"
                                else -> board.letters[cell].toString()
                            }
                            Surface(color = container, shape = RoundedCornerShape(12.dp), modifier = Modifier
                                .weight(1f).aspectRatio(1f)
                                .semantics { contentDescription = "Row ${row + 1}, column ${column + 1}, letter ${board.letters[cell]}" +
                                    when { found != null -> ", found in ${found.word}"; step >= 0 -> ", selected step ${step + 1}"; else -> "" } }
                                .clickable(enabled = canPlay) { onChange(StrandsRules.tap(board, progress, cell).toMap()) }) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(2.dp)) {
                                    Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            val selectedWord = StrandsRules.spelling(board, progress.selected)
            Text(if (selectedWord.isEmpty()) "Selected: —" else "Selected: $selectedWord", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = canPlay && progress.selected.isNotEmpty(), onClick = { onChange(StrandsRules.submit(board, progress).toMap()) }) { Text("Submit word") }
                OutlinedButton(enabled = canPlay && progress.selected.isNotEmpty(), onClick = { onChange(progress.copy(selected = emptyList(), message = "").toMap()) }) { Text("Clear path") }
            }
            if (progress.message.isNotBlank()) Text(progress.message, style = MaterialTheme.typography.bodyMedium)
            if (progress.found.isNotEmpty()) {
                Text("Found words", style = MaterialTheme.typography.titleSmall)
                progress.found.forEach { answer ->
                    Text(if (answer.word == board.spangram) "★ ${answer.word} · spanning answer" else "✓ ${answer.word}")
                }
            }
            TextButton(enabled = canPlay && progress.found.isNotEmpty(), onClick = { onChange(StrandsRules.undo(progress).toMap()) }) { Text("Undo last found word") }
            if (canPlay && progress.found.isNotEmpty()) Text("If a route blocks another answer, undo your last word and try a different route.", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
        }
    }
}
