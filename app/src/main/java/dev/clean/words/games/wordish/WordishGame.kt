package dev.clean.words.games.wordish

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import dev.clean.words.ui.GameActions
import kotlinx.coroutines.delay
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.clean.words.core.GameModule
import dev.clean.words.core.GameResult
import dev.clean.words.core.PlayStatus
import dev.clean.words.core.PuzzleRecord

object WordishGame : GameModule {
    override val id = "wordish"
    override val title = "Wordish"
    override val description = "Find the five-letter word in six guesses."
    override val puzzles = listOf("APPLE", "CRANE", "PLANT", "BRICK", "SHORE", "LIGHT", "MUSIC", "DREAM", "FLUTE", "GHOST").mapIndexed { index, answer ->
        PuzzleRecord("wordish-original-${index + 1}", title = "Original word ${index + 1}", data = mapOf(
            "answer" to answer, "dictionary" to WordishDictionary.version,
            "origin" to "Original CLEAN Words answer selection, 2026-10-25; accepted guesses derived from public-domain ENABLE"))
    }
    override fun result(puzzle: PuzzleRecord, state: Map<String, String>): GameResult = WordishRules.result(puzzle, state)
    override fun validate(puzzle: PuzzleRecord) = WordishRules.validate(puzzle)

    @Composable
    override fun Play(puzzle: PuzzleRecord, state: Map<String, String>, enabled: Boolean, onChange: (Map<String, String>) -> Unit) {
        // Immediate local reducer prevents rapid key callbacks reading a stale parent map.
        var live by remember(puzzle.id) { mutableStateOf(state) }
        var revealingRow by remember(puzzle.id) { mutableIntStateOf(-1) }
        var revealedColumns by remember(puzzle.id) { mutableIntStateOf(5) }
        fun dispatch(action: (Map<String, String>) -> Map<String, String>) {
            val next = action(live); live = next; onChange(next)
        }
        val guesses = WordishRules.guesses(live)
        val result = result(puzzle, live)
        val active = enabled && result.status == PlayStatus.IN_PROGRESS
        val keyboard = WordishRules.keyboardFeedback(puzzle, live)
        val darkText = Color(0xFF24332E)
        fun color(mark: LetterFeedback?) = when (mark) {
            LetterFeedback.CORRECT -> Color(0xFF99D8A5)
            LetterFeedback.ELSEWHERE -> Color(0xFFFFDC78)
            LetterFeedback.ABSENT -> Color(0xFFB6B9C3)
            null -> Color(0xFFE9E2FA)
        }
        LaunchedEffect(revealingRow, guesses.size) {
            if (revealingRow >= 0) {
                for (column in 1..5) { delay(85); revealedColumns = column }
                delay(180); revealingRow = -1
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(description)
            Text("✓ correct place   ~ elsewhere   × absent", style = MaterialTheme.typography.bodySmall)
            for (row in 0 until WordishRules.attempts) {
                val word = guesses.getOrNull(row) ?: if (row == guesses.size) WordishRules.draft(live) else ""
                val feedback = guesses.getOrNull(row)?.let { WordishRules.feedback(WordishRules.answer(puzzle), it) }
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                    for (i in 0..4) {
                        val shown = revealingRow != row || i < revealedColumns
                        val mark = feedback?.get(i)?.takeIf { shown }
                        val background by animateColorAsState(color(mark), tween(220), label = "tile-color")
                        val rotation by animateFloatAsState(if (shown) 0f else 85f, tween(210), label = "tile-reveal")
                        Box(modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                            .graphicsLayer { rotationX = rotation; cameraDistance = 12 * density }
                            .background(background, MaterialTheme.shapes.small).padding(4.dp), contentAlignment = Alignment.Center) {
                            Text("${word.getOrNull(i) ?: ' '}${mark?.let { "\n${it.symbol}" } ?: ""}", color = darkText, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            if (result.status == PlayStatus.IN_PROGRESS) {
                listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM").forEach { letters ->
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
                        letters.forEach { letter ->
                            val keyColor by animateColorAsState(color(keyboard[letter]), tween(250), label = "key-evidence")
                            OutlinedButton(onClick = { if (active) dispatch { WordishRules.input(puzzle, it, letter) } }, enabled = active,
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = keyColor, contentColor = darkText),
                                contentPadding = PaddingValues(0.dp), modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                                Text(letter.toString(), fontSize = 14.sp)
                            }
                        }
                    }
                }
                GameActions(onClear = { dispatch { WordishRules.clear(puzzle, it) } },
                    onEnter = {
                        val next = WordishRules.submit(puzzle, live)
                        if (WordishRules.guesses(next).size > guesses.size) { revealingRow = guesses.size; revealedColumns = 0 }
                        live = next; onChange(next)
                    }, onDelete = { dispatch { WordishRules.delete(puzzle, it) } }, enabled = active,
                    clearEnabled = WordishRules.draft(live).isNotEmpty(), deleteEnabled = WordishRules.draft(live).isNotEmpty())
            }
            live["message"]?.takeIf { it.isNotBlank() }?.let { Text(it) }
            Text(result.summary, fontWeight = if (result.status == PlayStatus.IN_PROGRESS) FontWeight.Normal else FontWeight.Bold)
        }
    }
}
