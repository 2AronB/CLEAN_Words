package dev.clean.words.games.crossword

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.clean.words.core.GameModule
import dev.clean.words.core.GameResult
import dev.clean.words.core.PlayStatus
import dev.clean.words.core.PuzzleRecord

object CrosswordGame : GameModule {
    override val id = "crossword"
    override val title = "Crossita"
    override val description = "Six crossing answers in a pocket-size 5×5 grid."
    override val puzzles = CrosswordContent.puzzles
    override fun validate(puzzle: PuzzleRecord) = CrosswordRules.validate(puzzle)
    override fun result(puzzle: PuzzleRecord, state: Map<String, String>): GameResult =
        CrosswordRules.board(puzzle)?.let { CrosswordRules.result(it, state) }
            ?: GameResult(summary = "This saved crossword could not be read")

    @Composable
    override fun Play(puzzle: PuzzleRecord, state: Map<String, String>, enabled: Boolean, onChange: (Map<String, String>) -> Unit) {
        val board = remember(puzzle) { CrosswordRules.board(puzzle) }
        if (board == null || CrosswordRules.validate(puzzle).isNotEmpty()) {
            Text("This saved crossword cannot be read. Its progress has not been changed.")
            return
        }
        // Updated synchronously inside input callbacks, before publishing to app persistence.
        // The app keys this composition by attempt ID so repeated content never shares edits.
        var progress by remember(puzzle.id, puzzle.revision) { mutableStateOf(state.toMap()) }
        var phoneKeyboard by remember { mutableStateOf(false) }
        val inputFocus = remember { FocusRequester() }
        val gridFocus = remember { FocusRequester() }
        val keyboard = LocalSoftwareKeyboardController.current
        val sentinel = "\u200B"
        var inputBuffer by remember { mutableStateOf(TextFieldValue(sentinel, TextRange(1))) }
        val selected = CrosswordInput.selected(board, progress)
        val current = CrosswordInput.current(board, progress)
        val entries = remember(board) { CrosswordInput.orderedEntries(board) }
        val cells = CrosswordRules.cells(board, progress)
        val checked = CrosswordRules.checked(progress)
        val completed = CrosswordRules.result(board, progress).status == PlayStatus.COMPLETED
        val editable = enabled && !completed
        val numbers = entries.associate { it.cells.first() to it.number }
        fun resetBuffer() { inputBuffer = TextFieldValue(sentinel, TextRange(1)) }
        fun publish(next: Map<String,String>) {
            if (!editable || next == progress) return
            progress = next
            onChange(next)
        }
        fun erase() {
            publish(CrosswordInput.delete(board, progress))
            val text = inputBuffer.text.filter { it in 'A'..'Z' }.dropLast(1)
            inputBuffer = TextFieldValue(sentinel + text, TextRange(text.length + 1))
        }
        fun enter() { publish(CrosswordInput.nextClue(board, progress)); resetBuffer() }
        fun select(at: Int, direction: CrosswordDirection) {
            // Completed history may be explored without changing saved moves.
            val next = CrosswordInput.select(board, progress, at, direction)
            if (editable) publish(next) else progress = next
            resetBuffer()
        }
        LaunchedEffect(phoneKeyboard) {
            if (phoneKeyboard) { inputFocus.requestFocus(); keyboard?.show() }
            else { keyboard?.hide(); gridFocus.requestFocus() }
        }
        val density = LocalDensity.current
        val windowHeight = with(density) { LocalWindowInfo.current.containerSize.height.toDp() }
        val keyboardHeight = with(density) { WindowInsets.ime.getBottom(this).toDp() }
        // Reserve space for the app header, clue and focused text field above the IME.
        val boardWidth = if (phoneKeyboard) (windowHeight - keyboardHeight - 290.dp).coerceIn(140.dp, 230.dp) else 300.dp
        Column(Modifier.focusRequester(gridFocus).onPreviewKeyEvent { event ->
            if (!editable || phoneKeyboard || event.type != KeyEventType.KeyDown) false
            else when {
                event.key == Key.Backspace || event.key == Key.Delete -> { erase(); true }
                event.key == Key.Enter || event.key == Key.NumPadEnter -> { enter(); true }
                !event.isCtrlPressed && !event.isAltPressed && !event.isMetaPressed -> {
                    val letter = event.nativeKeyEvent.unicodeChar.toChar()
                    if (letter.uppercaseChar() in 'A'..'Z') { publish(CrosswordInput.type(board, progress, letter.toString())); true } else false
                }
                else -> false
            }
        }.focusable(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Tap a square or clue to choose where to type.", style = MaterialTheme.typography.bodySmall)
            current?.let { entry ->
                Text("${entry.number} ${entry.direction.name.lowercase().replaceFirstChar { it.uppercase() }} · ${entry.clue} (${entry.cells.size})",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Column(Modifier.widthIn(max = boardWidth).fillMaxWidth().align(Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                            Box(Modifier.weight(1f).aspectRatio(1f).background(fill)
                                .border(if (active) 2.dp else 1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                                .then(if (blocked) Modifier else Modifier.clickable {
                                    val direction = CrosswordInput.direction(progress)
                                    val other = if (direction == CrosswordDirection.ACROSS) CrosswordDirection.DOWN else CrosswordDirection.ACROSS
                                    select(index, if (active && entries.any { index in it.cells && it.direction == other }) other else direction)
                                }).semantics {
                                    contentDescription = if (blocked) "Blocked square" else "Row ${row + 1}, column ${col + 1}, ${if (cells[index] == '.') "blank" else cells[index]}, ${if (wrong) "checked incorrect" else if (active) "selected" else ""}"
                                }, contentAlignment = Alignment.Center) {
                                if (!blocked) {
                                    numbers[index]?.let { Text(it.toString(), fontSize = 10.sp, lineHeight = 11.sp, modifier = Modifier.align(Alignment.TopStart).padding(2.dp)) }
                                    Text(if (cells[index] == '.') "" else cells[index].toString(), fontSize = if (phoneKeyboard) 20.sp else 24.sp,
                                        fontWeight = FontWeight.SemiBold, color = if (wrong) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface)
                                    if (wrong) Text("×", fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(1.dp))
                                }
                            }
                        }
                    }
                }
            }
            if (editable) {
                if (phoneKeyboard) {
                    OutlinedTextField(value = inputBuffer, onValueChange = { incoming ->
                        val edit = CrosswordInput.bufferEdit(inputBuffer.text, incoming.text)
                        var next = progress
                        repeat(edit.deletes) { next = CrosswordInput.delete(board, next) }
                        next = CrosswordInput.type(board, next, edit.inserted)
                        val normalized = incoming.text.uppercase(java.util.Locale.ROOT).filter { it in 'A'..'Z' }
                        inputBuffer = TextFieldValue(sentinel + normalized, TextRange(normalized.length + 1))
                        publish(next)
                    }, label = { Text("Type letters or paste; choose squares on the grid") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { enter() }),
                        modifier = Modifier.fillMaxWidth().focusRequester(inputFocus).onPreviewKeyEvent {
                            if (it.type == KeyEventType.KeyDown && (it.key == Key.Backspace || it.key == Key.Delete)) { erase(); true } else false
                        })
                } else {
                    listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM").forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            row.forEach { letter ->
                                OutlinedButton(onClick = { publish(CrosswordInput.type(board, progress, letter.toString())) },
                                    contentPadding = PaddingValues(0.dp), modifier = Modifier.weight(1f).heightIn(min = 44.dp)) { Text(letter.toString(), fontSize = 14.sp) }
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = { publish(CrosswordInput.clearClue(board, progress)); resetBuffer() }, modifier = Modifier.weight(1f)) { Text("Clear") }
                    Button(onClick = { enter() }, modifier = Modifier.weight(1f)) { Text("Enter") }
                    OutlinedButton(onClick = { erase() }, modifier = Modifier.weight(1f)) { Text("Delete") }
                }
                Text("Clear erases this clue. Enter moves to the next unfinished clue. Delete steps backward.", style = MaterialTheme.typography.bodySmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { phoneKeyboard = !phoneKeyboard; resetBuffer() }) { Text(if (phoneKeyboard) "Use puzzle keyboard" else "Use phone keyboard") }
                    OutlinedButton(onClick = { publish(CrosswordRules.check(board, progress)) }) { Text("Check entries") }
                }
                Text("Checking is optional. The puzzle keyboard keeps the board clear; phone keyboard also supports paste.", style = MaterialTheme.typography.bodySmall)
            } else if (completed) Text("✓ Crossword complete", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            if (progress["checkedOnce"] == "true") Text(if (checked.isEmpty()) "No entered mistakes currently marked." else "${checked.size} entered mistakes marked ×. Edit them to clear marks.", style = MaterialTheme.typography.bodySmall)
            CrosswordDirection.entries.forEach { direction ->
                Text(if (direction == CrosswordDirection.ACROSS) "Across" else "Down", style = MaterialTheme.typography.titleMedium)
                entries.filter { it.direction == direction }.forEach { entry ->
                    TextButton(onClick = { select(entry.cells.first(), direction) }, modifier = Modifier.fillMaxWidth()) {
                        Text("${if (entry == current) "▸ " else ""}${entry.number}. ${entry.clue} (${entry.cells.size})", modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}
