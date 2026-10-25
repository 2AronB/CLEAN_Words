package dev.clean.words.games.bee

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import dev.clean.words.core.GameModule
import dev.clean.words.core.GameResult
import dev.clean.words.core.PlayStatus
import dev.clean.words.core.PuzzleRecord
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object BeeGame : GameModule {
    override val id = "bee"
    override val title = "Spelling Meh"
    override val description = "Build words around one essential letter."
    override val puzzles = BeeContent.puzzles
    override fun result(puzzle: PuzzleRecord, state: Map<String, String>): GameResult =
        BeeRules.result(BeePuzzle.from(puzzle), BeeState.from(state))
    override fun validate(puzzle: PuzzleRecord): List<String> = BeeRules.validate(BeePuzzle.from(puzzle))

    @Composable
    override fun Play(puzzle: PuzzleRecord, state: Map<String, String>, enabled: Boolean, onChange: (Map<String, String>) -> Unit) {
        val board = remember(puzzle) { BeePuzzle.from(puzzle) }
        val input = remember(puzzle.id) { BeeInputBuffer(BeeState.from(state)) }
        var progress by remember(puzzle.id) { mutableStateOf(input.current) }
        SideEffect { progress = input.acceptExternal(BeeState.from(state)) }
        fun edit(transform: (BeeState) -> BeeState) {
            if (!enabled) return
            progress = input.edit(transform)
            onChange(progress.toMap())
        }
        val errors = remember(board) { BeeRules.validate(board) }
        if (errors.isNotEmpty()) {
            Text("This saved puzzle cannot be played: ${errors.joinToString()}. Your progress has not been erased.")
            return
        }
        val found = BeeRules.foundWords(board, progress)
        val outcome = BeeRules.result(board, progress)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Center letter: ${board.center}", style = MaterialTheme.typography.titleMedium)
            Text("Four letters or more. Always include the center. Letters may repeat.", style = MaterialTheme.typography.bodySmall)
            Honeycomb(board, enabled) { letter ->
                edit { it.copy(entry = (it.entry + letter).take(32), message = "") }
            }
            OutlinedTextField(value = progress.entry, onValueChange = { value ->
                edit { it.copy(entry = value.uppercase(Locale.ROOT).filter { c -> c in 'A'..'Z' }.take(32), message = "") }
            }, enabled = enabled, singleLine = true, label = { Text("Your word") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, autoCorrectEnabled = false, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { edit { BeeRules.submit(board, it) } }),
                modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(enabled = enabled && progress.entry.isNotEmpty(), onClick = { edit { it.copy(entry = "", message = "") } }, modifier = Modifier.weight(1f)) { Text("Clear") }
                Button(enabled = enabled && progress.entry.isNotBlank(), onClick = { edit { BeeRules.submit(board, it) } }, modifier = Modifier.weight(1f)) { Text("Enter") }
                OutlinedButton(enabled = enabled && progress.entry.isNotEmpty(), onClick = { edit { it.copy(entry = it.entry.dropLast(1), message = "") } }, modifier = Modifier.weight(1f)) { Text("Delete") }
            }
            AnimatedContent(targetState = progress.message, label = "Bee word feedback") { message ->
                if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            }
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.secondaryContainer) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val fraction = if (board.legacy) BeeRules.goalScore(board, progress).toFloat() / board.target
                        else found.size.toFloat() / board.wordTarget
                    val animated by animateFloatAsState(fraction.coerceIn(0f, 1f), tween(450), label = "Bee daily progress")
                    Text(if (board.legacy) "Saved puzzle · original completion rules" else "Daily goal: find ${board.wordTarget} words",
                        style = MaterialTheme.typography.titleMedium)
                    LinearProgressIndicator(progress = { animated }, modifier = Modifier.fillMaxWidth(),
                        trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = .18f))
                    Text(if (board.legacy) "This older puzzle keeps its original score and completion. New puzzles count every accepted word equally."
                        else "${found.size}/${board.wordTarget} words · every accepted word counts", style = MaterialTheme.typography.bodyMedium)
                    Text("${BeeRules.totalScore(board, progress)} points · ${found.size} words found", style = MaterialTheme.typography.labelLarge)
                }
            }
            if (outcome.status == PlayStatus.COMPLETED) Text("Daily goal reached ✓ Keep exploring if you like.", fontWeight = FontWeight.SemiBold)
            Text("Words by length", style = MaterialTheme.typography.titleMedium)
            Text("Each bar shows all accepted words of that length. You do not need every word to finish the daily goal.", style = MaterialTheme.typography.bodySmall)
            BeeRules.lengthProgress(board, progress).forEach { bucket ->
                val fraction by animateFloatAsState(bucket.found.toFloat() / bucket.total, tween(450), label = "${bucket.length}-letter progress")
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${bucket.length} letters", style = MaterialTheme.typography.bodyMedium)
                        Text("${bucket.found}/${bucket.total}", style = MaterialTheme.typography.labelLarge)
                    }
                    LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth())
                }
            }
            Text("Found words", style = MaterialTheme.typography.titleMedium)
            AnimatedContent(targetState = found.sorted().joinToString(" · ").ifEmpty { "None yet" }, label = "Found Bee words") {
                Text(it)
            }
            Text("Points: four letters = 1; longer words = their length. Using all seven letters adds 7.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

private val hexagon = GenericShape { size, _ ->
    repeat(6) { index ->
        val angle = -PI / 2 + index * PI / 3
        val x = size.width / 2 + size.width / 2 * cos(angle).toFloat()
        val y = size.height / 2 + size.height / 2 * sin(angle).toFloat()
        if (index == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

@Composable
private fun Honeycomb(board: BeePuzzle, enabled: Boolean, onLetter: (Char) -> Unit) {
    val letters = listOf(board.center) + board.letters.filter { it != board.center }.toList()
    val locations = remember { BeeGeometry.positions(6) }
    val colors = listOf(Color(0xFFFFD56B), Color(0xFFB9C8FF), Color(0xFFC5EBC6), Color(0xFFFFC3D4),
        Color(0xFFC7DDF6), Color(0xFFD8C2F0), Color(0xFFFFD4AF))
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val side = minOf(maxWidth, 330.dp)
        val cellSize = side * BeeGeometry.diameter(6)
        Box(Modifier.size(side)) {
            locations.forEachIndexed { index, position ->
                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                val scale by animateFloatAsState(if (pressed) .91f else 1f, tween(100), label = "Bee letter press")
                val color by animateColorAsState(if (pressed) colors[index].copy(alpha = .80f) else colors[index], tween(120), label = "Bee letter color")
                Surface(onClick = { onLetter(letters[index]) }, enabled = enabled,
                    interactionSource = interaction, shape = hexagon, color = color,
                    contentColor = Color(0xFF253044), shadowElevation = 3.dp,
                    modifier = Modifier.offset(x = side * position.x - cellSize / 2, y = side * position.y - cellSize / 2)
                        .size(cellSize).graphicsLayer(scaleX = scale, scaleY = scale)
                        .semantics { contentDescription = if (index == 0) "${letters[index]}, required center letter" else letters[index].toString() }) {
                    Box(contentAlignment = Alignment.Center) { Text(letters[index].toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
