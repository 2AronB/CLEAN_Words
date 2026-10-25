package dev.clean.words.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
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
