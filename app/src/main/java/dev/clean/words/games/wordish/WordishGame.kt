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
        val guesses = WordishRules.guesses(state)
        val result = result(puzzle, state)
        val active = enabled && result.status == PlayStatus.IN_PROGRESS
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(description)
            Text("✓ correct place   ~ elsewhere   × absent", style = MaterialTheme.typography.bodySmall)
            for (row in 0 until WordishRules.attempts) {
                val word = guesses.getOrNull(row) ?: if (row == guesses.size) WordishRules.draft(state) else ""
                val feedback = guesses.getOrNull(row)?.let { WordishRules.feedback(WordishRules.answer(puzzle), it) }
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                    for (i in 0..4) {
                        val mark = feedback?.get(i)
                        val background = when (mark) {
                            LetterFeedback.CORRECT -> MaterialTheme.colorScheme.primaryContainer
                            LetterFeedback.ELSEWHERE -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        Box(modifier = Modifier.weight(1f).heightIn(min = 52.dp).background(background, MaterialTheme.shapes.small).padding(4.dp), contentAlignment = Alignment.Center) {
                            Text("${word.getOrNull(i) ?: ' '}${mark?.let { "\n${it.symbol}" } ?: ""}", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            if (result.status == PlayStatus.IN_PROGRESS) {
                listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM").forEach { letters ->
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
                        letters.forEach { letter ->
                            OutlinedButton(onClick = { if (active) onChange(WordishRules.input(puzzle, state, letter)) }, enabled = active,
                                contentPadding = PaddingValues(0.dp), modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                                Text(letter.toString(), fontSize = 14.sp)
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { if (active) onChange(WordishRules.submit(puzzle, state)) }, enabled = active) { Text("Enter") }
                    OutlinedButton(onClick = { if (active) onChange(WordishRules.delete(puzzle, state)) }, enabled = active && WordishRules.draft(state).isNotEmpty()) { Text("Delete") }
                }
            }
            state["message"]?.takeIf { it.isNotBlank() }?.let { Text(it) }
            Text(result.summary, fontWeight = if (result.status == PlayStatus.IN_PROGRESS) FontWeight.Normal else FontWeight.Bold)
        }
    }
}
