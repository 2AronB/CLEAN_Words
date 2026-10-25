package dev.clean.words.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GameMessage(text: String) {
    if (text.isNotBlank()) Text(text, style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
fun LetterKey(label: String, onClick: () -> Unit, enabled: Boolean = true, selected: Boolean = false) {
    OutlinedButton(onClick = onClick, enabled = enabled) {
        Text(if (selected) "[$label]" else label)
    }
}

@Composable
fun GamePanel(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

/** Draft controls only: never clear accepted play or history. */
@Composable
fun GameActions(
    onClear: () -> Unit, onEnter: () -> Unit, onDelete: () -> Unit,
    enabled: Boolean = true, clearEnabled: Boolean = true,
    enterEnabled: Boolean = true, deleteEnabled: Boolean = true,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onClear, enabled = enabled && clearEnabled, modifier = Modifier.weight(1f)) { Text("Clear") }
        Button(onClick = onEnter, enabled = enabled && enterEnabled, modifier = Modifier.weight(1f)) { Text("Enter") }
        OutlinedButton(onClick = onDelete, enabled = enabled && deleteEnabled, modifier = Modifier.weight(1f)) { Text("Delete") }
    }
}
