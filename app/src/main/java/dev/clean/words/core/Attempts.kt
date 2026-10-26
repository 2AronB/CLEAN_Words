package dev.clean.words.core

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import dev.clean.words.games.bee.BeeOpening
import dev.clean.words.games.bee.BeeState

enum class PuzzleMode { DAILY, EXTRA }

data class Attempt(
    val id: String,
    val gameId: String,
    val mode: PuzzleMode,
    val puzzleDate: String?,
    val puzzle: PuzzleRecord,
    val state: Map<String, String> = emptyMap(),
    val startedAt: String,
    val updatedAt: String,
    val completedAt: String? = null,
    val result: GameResult = GameResult(),
)

data class Library(val attempts: List<Attempt> = emptyList())

object AttemptRules {
    fun puzzleDate(instant: Instant, zone: ZoneId): LocalDate {
        val local = instant.atZone(zone)
        return local.toLocalDate().minusDays(if (local.hour < 6) 1 else 0)
    }

    fun dailyIndex(gameId: String, date: LocalDate, size: Int): Int {
        require(size > 0)
        return Math.floorMod(date.toEpochDay() + gameId.hashCode().toLong(), size.toLong()).toInt()
    }

    fun daily(library: Library, game: GameModule, date: LocalDate): Attempt? =
        library.attempts.firstOrNull { it.gameId == game.id && it.mode == PuzzleMode.DAILY && it.puzzleDate == date.toString() }

    fun create(library: Library, game: GameModule, mode: PuzzleMode, now: Instant, zone: ZoneId): Attempt {
        val date = puzzleDate(now, zone)
        if (mode == PuzzleMode.DAILY) daily(library, game, date)?.let { return it }
        val index = if (mode == PuzzleMode.DAILY) dailyIndex(game.id, date, game.puzzles.size)
            else library.attempts.count { it.gameId == game.id && it.mode == PuzzleMode.EXTRA } % game.puzzles.size
        val puzzle = game.puzzles[index]
        return createSnapshot(library, game, mode, puzzle, now, zone)
    }

    /** Generated starts supply complete frozen content; resume always wins over new content. */
    fun createSnapshot(library: Library, game: GameModule, mode: PuzzleMode, puzzle: PuzzleRecord,
        now: Instant, zone: ZoneId, attemptId: String = UUID.randomUUID().toString()): Attempt {
        val date = puzzleDate(now, zone)
        if (mode == PuzzleMode.DAILY) daily(library, game, date)?.let { return it }
        require(game.validate(puzzle).isEmpty()) { "Puzzle content failed validation" }
        return Attempt(attemptId, game.id, mode,
            if (mode == PuzzleMode.DAILY) date.toString() else null, puzzle,
            startedAt = now.toString(), updatedAt = now.toString(), result = game.result(puzzle, emptyMap()))
    }

    fun update(attempt: Attempt, state: Map<String, String>, game: GameModule, now: Instant): Attempt {
        require(AttemptPresentation.inspect(attempt).errors.isEmpty() &&
            AttemptPresentation.inspect(attempt.copy(state = state)).errors.isEmpty()) {
            "Saved Bee opening needs recovery; original state and result were preserved"
        }
        if (attempt.gameId == "bee" && attempt.puzzle.data["scoringVersion"] == BeeOpening.SCORING) {
            val before = BeeState.from(attempt.state)
            val after = BeeState.from(state)
            val oldOrder = before.acceptedOrder.orEmpty()
            require(after.acceptedOrder.orEmpty().take(oldOrder.size) == oldOrder &&
                after.acceptedOrder.orEmpty().size >= oldOrder.size && after.editRevision >= before.editRevision &&
                (before.editRevision == 0L || after.editRevision > before.editRevision || after == before)) {
                "Stale or conflicting Bee edit; accepted words and recorded opening were preserved"
            }
        }
        val result = game.result(attempt.puzzle, state)
        val completed = attempt.completedAt ?: if (result.status != PlayStatus.IN_PROGRESS) now.toString() else null
        val sticky = if (attempt.result.status == PlayStatus.COMPLETED && result.status == PlayStatus.IN_PROGRESS)
            result.copy(status = PlayStatus.COMPLETED) else result
        return attempt.copy(state = state.toMap(), updatedAt = now.toString(), completedAt = completed, result = sticky)
    }

    fun upsert(library: Library, attempt: Attempt): Library {
        val existing = library.attempts.indexOfFirst { it.id == attempt.id }
        return if (existing < 0) Library(library.attempts + attempt)
        else Library(library.attempts.toMutableList().also { it[existing] = attempt })
    }

    fun previousUnfinished(library: Library, gameId: String, today: LocalDate): Attempt? =
        library.attempts.filter { it.gameId == gameId && it.mode == PuzzleMode.DAILY &&
            it.puzzleDate != null && it.puzzleDate < today.toString() && AttemptPresentation.inspect(it).needsResume }
            .maxByOrNull { it.puzzleDate ?: "" }
}
