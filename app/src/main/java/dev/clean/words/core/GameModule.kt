package dev.clean.words.core

import androidx.compose.runtime.Composable

data class PuzzleRecord(
    val id: String,
    val revision: Int = 1,
    val title: String,
    val data: Map<String, String>,
)

enum class PlayStatus { IN_PROGRESS, COMPLETED, FAILED }

data class GameResult(
    val status: PlayStatus = PlayStatus.IN_PROGRESS,
    val summary: String = "In progress",
    val score: Int? = null,
    val maxScore: Int? = null,
    val assisted: Boolean = false,
)

interface GameModule {
    val id: String
    val title: String
    val description: String
    val puzzles: List<PuzzleRecord>
    fun result(puzzle: PuzzleRecord, state: Map<String, String>): GameResult
    fun validate(puzzle: PuzzleRecord): List<String>
    @Composable
    fun Play(
        puzzle: PuzzleRecord,
        state: Map<String, String>,
        enabled: Boolean,
        onChange: (Map<String, String>) -> Unit,
    )
}
