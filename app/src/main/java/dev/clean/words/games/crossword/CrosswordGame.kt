package dev.clean.words.games.crossword

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.clean.words.core.GameModule
import dev.clean.words.core.GameResult
import dev.clean.words.core.PlayStatus
import dev.clean.words.core.PuzzleRecord

object CrosswordGame : GameModule {
    override val id = "crossword"
    override val title = "Minuscule Crossword"
    override val description = "Six crossing answers in a pocket-size 5×5 grid."
    override val puzzles = CrosswordContent.puzzles

    override fun validate(puzzle: PuzzleRecord) = CrosswordRules.validate(puzzle)

    override fun result(puzzle: PuzzleRecord, state: Map<String, String>): GameResult {
        val board = CrosswordRules.board(puzzle)
            ?: return GameResult(summary = "This saved crossword could not be read")
        return CrosswordRules.result(board, state)
    }

    @Composable
    override fun Play(puzzle: PuzzleRecord, state: Map<String, String>, enabled: Boolean, onChange: (Map<String, String>) -> Unit) {
        val board = CrosswordRules.board(puzzle)
        if (board == null) {
            Text("This saved crossword could not be read. Your saved attempt has not been changed.")
            return
        }
        val entries = remember(board) { board.entries() }
        if (entries.isEmpty() || CrosswordRules.validate(puzzle).isNotEmpty()) {
            Text("This saved crossword has invalid content. Your saved attempt has not been changed.")
            return
        }
        var selected by remember(puzzle.id) { mutableIntStateOf(
            state["selected"]?.toIntOrNull()?.takeIf { it in board.grid.indices && board.grid[it] != '#' }
                ?: board.grid.indexOfFirst { it != '#' }
        ) }
        var direction by remember(puzzle.id) { mutableStateOf(
            CrosswordDirection.entries.firstOrNull { it.name == state["direction"] } ?: CrosswordDirection.ACROSS
        ) }
        val current = entries.firstOrNull { selected in it.cells && it.direction == direction }
            ?: entries.firstOrNull { selected in it.cells }
        val cells = CrosswordRules.cells(board, state)
        val checked = CrosswordRules.checked(state)
        val completed = CrosswordRules.result(board, state).status == PlayStatus.COMPLETED
        val editable = enabled && !completed
        val numbers = entries.associate { it.cells.first() to it.number }

        fun select(index: Int, wanted: CrosswordDirection) {
            selected = index
            direction = wanted
            if (editable) onChange(state + mapOf("selected" to index.toString(), "direction" to wanted.name))
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Tap a square or clue, then enter a letter. All crossings share the same square.",
                style = MaterialTheme.typography.bodyMedium)
            if (completed) Text("✓ All answers correct", fontWeight = FontWeight.Bold)
            Column(Modifier.widthIn(max = 360.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(5) { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(5) { col ->
                            val index = row * 5 + col
                            val blocked = board.grid[index] == '#'
                            val active = index == selected
                            val inWord = current?.cells?.contains(index) == true
                            val wrong = index in checked
                            val fill = when {
                                blocked -> MaterialTheme.colorScheme.onSurface
                                wrong -> MaterialTheme.colorScheme.errorContainer
                                active -> MaterialTheme.colorScheme.primaryContainer
                                inWord -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            Box(
                                Modifier.weight(1f).aspectRatio(1f).background(fill)
                                    .border(if (active) 2.dp else 1.dp,
                                        if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                                    .then(if (blocked) Modifier else Modifier.clickable {
                                        val alternate = if (direction == CrosswordDirection.ACROSS) CrosswordDirection.DOWN else CrosswordDirection.ACROSS
                                        val wanted = if (active && entries.any { index in it.cells && it.direction == alternate }) alternate
                                            else entries.firstOrNull { index in it.cells && it.direction == direction }?.direction
                                                ?: entries.first { index in it.cells }.direction
                                        select(index, wanted)
                                    })
                                    .semantics {
                                        contentDescription = if (blocked) "Blocked square" else
                                            "Row ${row + 1}, column ${col + 1}, ${if (cells[index] == '.') "blank" else cells[index]}, ${if (wrong) "checked incorrect" else if (active) "selected" else ""}"
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (!blocked) {
                                    numbers[index]?.let {
                                        Text(it.toString(), fontSize = 10.sp, lineHeight = 11.sp,
                                            modifier = Modifier.align(Alignment.TopStart).padding(2.dp))
                                    }
                                    Text(if (cells[index] == '.') "" else cells[index].toString(),
                                        fontSize = 24.sp, fontWeight = FontWeight.SemiBold,
                                        color = if (wrong) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface)
                                    if (wrong) Text("×", fontSize = 13.sp,
                                        modifier = Modifier.align(Alignment.BottomEnd).padding(1.dp),
                                        color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
            current?.let { entry ->
                Text("${entry.number} ${entry.direction.name.lowercase().replaceFirstChar { it.uppercase() }} · ${entry.clue} (${entry.cells.size})",
                    fontWeight = FontWeight.SemiBold)
                if (editable) {
                    val letter = cells[selected].takeUnless { it == '.' }?.toString().orEmpty()
                    OutlinedTextField(
                        value = TextFieldValue(letter, TextRange(0, letter.length)),
                        onValueChange = { value ->
                            val input = value.text.uppercase(java.util.Locale.ROOT)
                            if (input.isEmpty() || input.all { it in 'A'..'Z' }) {
                                val character = input.takeLast(1)
                                val updated = CrosswordRules.enter(board, state, selected, character)
                                val next = if (character.isEmpty()) selected else CrosswordRules.nextCell(entry, selected)
                                selected = next
                                onChange(updated + mapOf("selected" to next.toString(), "direction" to entry.direction.name))
                            }
                        },
                        label = { Text("Letter at row ${selected / 5 + 1}, column ${selected % 5 + 1}") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters,
                            autoCorrectEnabled = false, keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { select(CrosswordRules.nextCell(entry, selected), entry.direction) }),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onChange(CrosswordRules.enter(board, state, selected, "")) }) { Text("Clear square") }
                        Button(onClick = { onChange(CrosswordRules.check(board, state)) }) { Text("Check entries") }
                    }
                    Text("Check marks entered mistakes with × and records assistance. It never erases letters.",
                        style = MaterialTheme.typography.bodySmall)
                    if (state["checkedOnce"] == "true") Text(
                        if (checked.isEmpty()) "No entered mistakes currently marked." else "${checked.size} entered mistake(s) marked ×.",
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
            CrosswordDirection.entries.forEach { clueDirection ->
                Spacer(Modifier.height(4.dp))
                Text(if (clueDirection == CrosswordDirection.ACROSS) "Across" else "Down", style = MaterialTheme.typography.titleMedium)
                entries.filter { it.direction == clueDirection }.forEach { entry ->
                    TextButton(onClick = { select(entry.cells.first(), entry.direction) }, modifier = Modifier.fillMaxWidth()) {
                        Text("${if (entry == current) "▸ " else ""}${entry.number}. ${entry.clue} (${entry.cells.size})",
                            modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}
