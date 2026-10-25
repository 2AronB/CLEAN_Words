package dev.clean.words.games.wordish

import dev.clean.words.core.GameResult
import dev.clean.words.core.PlayStatus
import dev.clean.words.core.PuzzleRecord

enum class LetterFeedback(val symbol: String, val meaning: String) {
    CORRECT("✓", "correct place"), ELSEWHERE("~", "elsewhere"), ABSENT("×", "absent")
}

object WordishRules {
    const val attempts = 6
    fun answer(puzzle: PuzzleRecord) = puzzle.data["answer"].orEmpty()
    fun guesses(state: Map<String, String>): List<String> = state["guesses"].orEmpty().split(',').filter { it.matches(Regex("[A-Z]{5}")) }.take(attempts)
    fun draft(state: Map<String, String>) = state["draft"].orEmpty().filter { it in 'A'..'Z' }.take(5)
    fun isAccepted(word: String) = word in WordishDictionary.words

    fun feedback(answer: String, guess: String): List<LetterFeedback> {
        require(answer.length == 5 && guess.length == 5) { "Feedback needs five-letter strings" }
        val result = MutableList(5) { LetterFeedback.ABSENT }
        val counts = mutableMapOf<Char, Int>()
        for (i in 0..4) {
            if (answer[i] == guess[i]) result[i] = LetterFeedback.CORRECT
            else counts[answer[i]] = (counts[answer[i]] ?: 0) + 1
        }
        for (i in 0..4) if (result[i] != LetterFeedback.CORRECT && (counts[guess[i]] ?: 0) > 0) {
            result[i] = LetterFeedback.ELSEWHERE
            counts[guess[i]] = counts.getValue(guess[i]) - 1
        }
        return result
    }

    fun result(puzzle: PuzzleRecord, state: Map<String, String>): GameResult {
        val guesses = guesses(state)
        val won = guesses.indexOf(answer(puzzle))
        return when {
            won >= 0 -> GameResult(PlayStatus.COMPLETED, "Solved in ${won + 1} of 6 guesses", won + 1, attempts)
            guesses.size >= attempts -> GameResult(PlayStatus.FAILED, "Out of guesses. Answer: ${answer(puzzle)}", attempts, attempts)
            else -> GameResult(PlayStatus.IN_PROGRESS, "${guesses.size} of 6 guesses used", guesses.size, attempts)
        }
    }

    fun input(puzzle: PuzzleRecord, state: Map<String, String>, letter: Char): Map<String, String> {
        if (result(puzzle, state).status != PlayStatus.IN_PROGRESS || letter.uppercaseChar() !in 'A'..'Z' || draft(state).length >= 5) return state
        return state + mapOf("draft" to draft(state) + letter.uppercaseChar(), "message" to "")
    }

    fun delete(puzzle: PuzzleRecord, state: Map<String, String>): Map<String, String> =
        if (result(puzzle, state).status != PlayStatus.IN_PROGRESS) state else state + mapOf("draft" to draft(state).dropLast(1), "message" to "")

    fun submit(puzzle: PuzzleRecord, state: Map<String, String>): Map<String, String> {
        if (result(puzzle, state).status != PlayStatus.IN_PROGRESS) return state
        val word = draft(state)
        if (word.length != 5) return state + ("message" to "Enter five letters. No guess used.")
        if (!isAccepted(word)) return state + ("message" to "Not in the bundled guess dictionary. No guess used.")
        if (word in guesses(state)) return state + ("message" to "You already guessed $word. No guess used.")
        return state + mapOf("guesses" to (guesses(state) + word).joinToString(","), "draft" to "", "message" to "")
    }

    fun validate(puzzle: PuzzleRecord): List<String> = buildList {
        if (puzzle.id.isBlank()) add("Missing puzzle identity")
        if (!answer(puzzle).matches(Regex("[A-Z]{5}"))) add("Answer must contain five uppercase English letters")
        if (!isAccepted(answer(puzzle))) add("Answer is missing from accepted dictionary")
        if (puzzle.data["dictionary"] != WordishDictionary.version) add("Unrecognized dictionary version")
        if (puzzle.data["origin"].isNullOrBlank()) add("Missing provenance")
    }
}
