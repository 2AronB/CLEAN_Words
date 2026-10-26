package dev.clean.words.core

import dev.clean.words.games.bee.BeeOpening
import dev.clean.words.games.bee.BeeOpeningProgress
import dev.clean.words.games.bee.BeePuzzle
import dev.clean.words.games.bee.BeeRules
import dev.clean.words.games.bee.BeeState

data class AttemptProgress(
    val label: String,
    val needsResume: Boolean,
    val calendarMark: String,
    val opening: BeeOpeningProgress? = null,
    val errors: List<String> = emptyList(),
) {
    val openingLabel: String? get() = opening?.let {
        if (it.recorded) "Opening score: ${it.percent.toPlainString()}% · recorded"
        else "Opening score: ${it.percent.toPlainString()}% · provisional (${it.words.size}/10)"
    }
    val goalLabel: String? get() = opening?.let {
        "Word goal: ${it.foundCount}/${it.completionGoal}${if (it.goalReached) " · reached" else ""}"
    }
}

/** Versioned Bee milestones are derived from frozen content/order, never from a second stored score. */
object AttemptPresentation {
    fun inspect(attempt: Attempt): AttemptProgress {
        if (attempt.gameId != "bee" || (attempt.puzzle.data["scoringVersion"] != BeeOpening.SCORING && attempt.puzzle.revision < 3)) {
            return when (attempt.result.status) {
                PlayStatus.IN_PROGRESS -> AttemptProgress("In progress", true, "…")
                PlayStatus.COMPLETED -> AttemptProgress("Completed", false, "✓")
                PlayStatus.FAILED -> AttemptProgress("Finished · not solved", false, "×")
            }
        }
        return runCatching {
            val board = BeePuzzle.from(attempt.puzzle)
            val state = BeeState.from(attempt.state)
            val errors = (BeeRules.validate(board) + BeeOpening.stateErrors(board, state)).distinct()
            if (errors.isNotEmpty()) AttemptProgress("Saved Bee needs recovery", true, "!", errors = errors)
            else {
                val p = BeeOpening.progress(board, state)
                val label = when {
                    p.recorded && p.goalReached -> "Opening recorded · word goal reached"
                    p.goalReached -> "Word goal reached · opening unfinished (${p.words.size}/10)"
                    p.recorded -> "Opening recorded · word goal unfinished"
                    else -> "Opening ${p.words.size}/10 · word goal unfinished"
                }
                AttemptProgress(label, !p.recorded || !p.goalReached,
                    if (p.recorded && p.goalReached) "✓" else "…", p)
            }
        }.getOrElse { AttemptProgress("Saved Bee needs recovery", true, "!",
            errors = listOf("Saved Bee opening cannot be interpreted: ${it.message}")) }
    }
}
