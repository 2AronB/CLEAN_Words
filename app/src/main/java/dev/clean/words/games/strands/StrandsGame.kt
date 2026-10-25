package dev.clean.words.games.strands

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.clean.words.core.GameModule
import dev.clean.words.core.GameResult
import dev.clean.words.core.PlayStatus
import dev.clean.words.core.PuzzleRecord
import kotlin.math.floor

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
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(board.clue, style = MaterialTheme.typography.titleLarge)
            Text("Find ${board.answers.size} theme words. One spanning answer joins opposite edges.", style = MaterialTheme.typography.bodyMedium)
            Text("Tap neighboring letters, including diagonals, then Enter. Lines show each route.", style = MaterialTheme.typography.bodySmall)
            Text(outcome.summary, fontWeight = FontWeight.SemiBold)
            StrandBoard(board, progress, canPlay) { cell -> onChange(StrandsRules.tap(board, progress, cell).toMap()) }
            val selectedWord = StrandsRules.spelling(board, progress.selected)
            Text(if (selectedWord.isEmpty()) "Selected: —" else "Selected: $selectedWord", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(enabled = canPlay && progress.selected.isNotEmpty(),
                    onClick = { onChange(StrandsRules.clear(progress).toMap()) }, modifier = Modifier.weight(1f)) { Text("Clear") }
                Button(enabled = canPlay && progress.selected.isNotEmpty(),
                    onClick = { onChange(StrandsRules.submit(board, progress).toMap()) }, modifier = Modifier.weight(1f)) { Text("Enter") }
                OutlinedButton(enabled = canPlay && progress.selected.isNotEmpty(),
                    onClick = { onChange(StrandsRules.delete(progress).toMap()) }, modifier = Modifier.weight(1f)) { Text("Delete") }
            }
            if (progress.message.isNotBlank()) Text(progress.message, style = MaterialTheme.typography.bodyMedium)
            if (progress.found.isNotEmpty()) {
                Text("Found words", style = MaterialTheme.typography.titleSmall)
                progress.found.forEach { answer ->
                    Text(if (answer.word == board.spangram) "★ ${answer.word} · spanning answer" else "✓ ${answer.word}")
                }
            }
            TextButton(enabled = canPlay && progress.found.isNotEmpty(), onClick = { onChange(StrandsRules.undo(progress).toMap()) }) {
                Text("Undo last found word")
            }
            if (canPlay) Text("Alternate routes are welcome when the remaining theme words still fit. Undo can repair an older saved route.",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

private val StrandInk = Color(0xFF27344A)
private val StrandNeutral = Color(0xFFEDE7FC)
private val StrandSelected = Color(0xFFBFD5FF)
private val StrandSelectedLine = Color(0xFF789FDB)
private val StrandSpan = Color(0xFFFFDA83)
private val StrandSpanLine = Color(0xFFE3AF41)
private val StrandFound = listOf(Color(0xFFBFE8D5), Color(0xFFFFD3C4), Color(0xFFE4CBFA), Color(0xFFBEE8EE))
private val StrandFoundLine = listOf(Color(0xFF7CBCA5), Color(0xFFE8A693), Color(0xFFB394D8), Color(0xFF80BFC9))

@Composable
private fun StrandBoard(board: StrandsPuzzle, progress: StrandsState, enabled: Boolean, onCell: (Int) -> Unit) {
    val colors = board.letters.indices.map { cell ->
        val foundIndex = progress.found.indexOfFirst { cell in it.path }
        val isSpan = foundIndex >= 0 && progress.found[foundIndex].word == board.spangram
        val color = when {
            isSpan -> StrandSpan
            foundIndex >= 0 -> StrandFound[foundIndex % StrandFound.size]
            cell in progress.selected -> StrandSelected
            else -> StrandNeutral
        }
        animateColorAsState(color, tween(220), label = "strand-cell-$cell").value
    }
    val foundReveals = progress.found.map { answer ->
        key(answer.word, answer.path) {
            val reveal = remember { Animatable(0f) }
            LaunchedEffect(answer.word, answer.path) { reveal.animateTo(1f, tween(420)) }
            reveal.value
        }
    }
    val selectedSegments by animateFloatAsState((progress.selected.size - 1).coerceAtLeast(0).toFloat(), tween(130), label = "strand-selection-line")
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val gap = 4.dp
        val cellWidth = (maxWidth - gap * (board.columns - 1)) / board.columns
        val boardHeight = cellWidth * board.rows + gap * (board.rows - 1)
        Box(Modifier.fillMaxWidth().height(boardHeight)) {
            Canvas(Modifier.fillMaxSize()) {
                val spacing = gap.toPx()
                val cell = (size.width - spacing * (board.columns - 1)) / board.columns
                fun center(index: Int) = Offset((index % board.columns) * (cell + spacing) + cell / 2,
                    (index / board.columns) * (cell + spacing) + cell / 2)
                board.letters.indices.forEach { index ->
                    drawRoundRect(colors[index], topLeft = Offset((index % board.columns) * (cell + spacing),
                        (index / board.columns) * (cell + spacing)), size = Size(cell, cell), cornerRadius = CornerRadius(12.dp.toPx()))
                }
                fun route(path: List<Int>, color: Color, segments: Float) {
                    if (path.isEmpty()) return
                    val full = floor(segments).toInt().coerceIn(0, (path.size - 1).coerceAtLeast(0))
                    val fraction = (segments - full).coerceIn(0f, 1f)
                    val width = (cell * 0.32f).coerceAtLeast(12.dp.toPx())
                    for (i in 0 until full) drawLine(color, center(path[i]), center(path[i + 1]), width, StrokeCap.Round)
                    if (full < path.lastIndex && fraction > 0f) {
                        val start = center(path[full]); val end = center(path[full + 1])
                        drawLine(color, start, start + (end - start) * fraction, width, StrokeCap.Round)
                    }
                    drawCircle(color, radius = width / 2, center = center(path.first()))
                }
                progress.found.forEachIndexed { index, answer ->
                    route(answer.path, if (answer.word == board.spangram) StrandSpanLine else StrandFoundLine[index % StrandFoundLine.size],
                        (answer.path.size - 1) * foundReveals[index])
                }
                route(progress.selected, StrandSelectedLine, selectedSegments)
            }
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(gap)) {
                repeat(board.rows) { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                        repeat(board.columns) { column ->
                            val cell = row * board.columns + column
                            val found = progress.found.firstOrNull { cell in it.path }
                            val step = progress.selected.indexOf(cell)
                            Box(Modifier.weight(1f).aspectRatio(1f).semantics {
                                contentDescription = "Row ${row + 1}, column ${column + 1}, letter ${board.letters[cell]}" +
                                    when { found != null -> ", found in ${found.word}"; step >= 0 -> ", selected step ${step + 1}"; else -> "" }
                            }.clickable(enabled = enabled) { onCell(cell) }.padding(2.dp), contentAlignment = Alignment.Center) {
                                Text(board.letters[cell].toString(), style = MaterialTheme.typography.titleLarge,
                                    color = StrandInk, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
