package dev.clean.words.games.bee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import dev.clean.words.core.GameModule
import dev.clean.words.core.GameResult
import dev.clean.words.core.PuzzleRecord
import java.util.Locale

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
        val board = BeePuzzle.from(puzzle)
        val progress = BeeState.from(state)
        val errors = BeeRules.validate(board)
        if (errors.isNotEmpty()) {
            Text("This saved puzzle cannot be played: ${errors.joinToString()}. Your progress has not been erased.")
            return
        }
        val goal = BeeRules.goalScore(board, progress)
        val bonus = BeeRules.bonusScore(board, progress)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Center letter: ${board.center}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Use at least four letters and always include ${board.center}. Letters may repeat.")
            val outside = board.letters.filter { it != board.center }
            repeat(2) { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    outside.substring(row * 3, row * 3 + 3).forEach { letter ->
                        OutlinedButton(enabled = enabled, onClick = { onChange(progress.copy(entry = progress.entry + letter, message = "").toMap()) }, modifier = Modifier.weight(1f)) { Text(letter.toString(), fontWeight = FontWeight.Bold) }
                    }
                }
            }
            Button(enabled = enabled, onClick = { onChange(progress.copy(entry = progress.entry + board.center, message = "").toMap()) }, modifier = Modifier.fillMaxWidth()) { Text("${board.center} · required center letter", fontWeight = FontWeight.Bold) }
            OutlinedTextField(value = progress.entry, onValueChange = { value ->
                onChange(progress.copy(entry = value.uppercase(Locale.ROOT).take(32), message = "").toMap())
            }, enabled = enabled, singleLine = true, label = { Text("Your word") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (enabled) onChange(BeeRules.submit(board, progress).toMap()) }),
                modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = enabled && progress.entry.isNotBlank(), onClick = { onChange(BeeRules.submit(board, progress).toMap()) }) { Text("Submit word") }
                OutlinedButton(enabled = enabled && progress.entry.isNotEmpty(), onClick = { onChange(progress.copy(entry = progress.entry.dropLast(1), message = "").toMap()) }) { Text("Delete") }
                OutlinedButton(enabled = enabled && progress.entry.isNotEmpty(), onClick = { onChange(progress.copy(entry = "", message = "").toMap()) }) { Text("Clear") }
            }
            if (progress.message.isNotBlank()) Text(progress.message)
            Surface(shape = MaterialTheme.shapes.large, tonalElevation = 2.dp) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Goal: $goal / ${board.target} points", style = MaterialTheme.typography.titleMedium)
                    LinearProgressIndicator(progress = { (goal.toFloat() / board.target).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                    Text("${board.goalWords.count { it in progress.found }} / ${board.goalWords.size} target words found · $bonus bonus points")
                    Text("This starter's target is 70% of ${board.maximumGoalScore} points from a small familiar-word list. Other ENABLE words count as bonus; obscure words are never required for this target.", style = MaterialTheme.typography.bodySmall)
                }
            }
            Text("Scoring: four-letter words = 1; longer words = their length; using all seven letters adds 7.", style = MaterialTheme.typography.bodySmall)
            if (goal >= board.target) Text("Target reached ✓ Keep playing toward all target words and bonus words.", fontWeight = FontWeight.SemiBold)
            Text("Found target words", style = MaterialTheme.typography.titleSmall)
            Text(progress.found.filter { it in board.goalWords }.sorted().joinToString(" · ").ifEmpty { "None yet" })
            val bonusWords = progress.found.filter { it !in board.goalWords && it in board.validWords }.sorted()
            if (bonusWords.isNotEmpty()) {
                Text("Bonus words", style = MaterialTheme.typography.titleSmall)
                Text(bonusWords.joinToString(" · "))
            }
        }
    }
}
