package dev.clean.words.games.wordish

import dev.clean.words.core.PlayStatus
import org.junit.Assert.*
import org.junit.Test

class WordishRulesTest {
    private val puzzle = WordishGame.puzzles.first()
    private fun enter(word: String, state: Map<String, String> = emptyMap()) = word.fold(state) { s, c -> WordishRules.input(puzzle, s, c) }

    @Test fun tenDistinctAnswersAndIdentitiesValidate() {
        assertEquals(10, WordishGame.puzzles.size)
        assertEquals(10, WordishGame.puzzles.map { it.id }.distinct().size)
        assertEquals(10, WordishGame.puzzles.map { WordishRules.answer(it) }.distinct().size)
        WordishGame.puzzles.forEach { assertEquals(it.id, emptyList<String>(), WordishRules.validate(it)) }
    }
    @Test fun broadAcceptedDictionaryIsBundledAndVersioned() {
        assertEquals(8636, WordishDictionary.words.size)
        assertTrue(WordishRules.isAccepted("ZEBRA"))
        assertFalse(WordishRules.isAccepted("ZZZZZ"))
        assertTrue(WordishDictionary.words.all { it.matches(Regex("[A-Z]{5}")) })
    }
    @Test fun duplicateLettersDoNotReceiveExtraYellow() {
        assertEquals(listOf(LetterFeedback.CORRECT, LetterFeedback.ELSEWHERE, LetterFeedback.ABSENT, LetterFeedback.ELSEWHERE, LetterFeedback.ABSENT), WordishRules.feedback("APPLE", "ALLEY"))
        assertEquals(listOf(LetterFeedback.ELSEWHERE, LetterFeedback.ELSEWHERE, LetterFeedback.CORRECT, LetterFeedback.CORRECT, LetterFeedback.ABSENT), WordishRules.feedback("ABBEY", "BABES"))
    }
    @Test fun shortAndUnknownGuessesDoNotUseAttemptOrLoseDraft() {
        val short = WordishRules.submit(puzzle, enter("APP"))
        assertEquals(emptyList<String>(), WordishRules.guesses(short))
        assertEquals("APP", WordishRules.draft(short))
        val invalid = WordishRules.submit(puzzle, enter("ZZZZZ"))
        assertEquals(emptyList<String>(), WordishRules.guesses(invalid))
        assertEquals("ZZZZZ", WordishRules.draft(invalid))
    }
    @Test fun draftInputIsBoundedAndCanBeCorrected() {
        val full = enter("cranes")
        assertEquals("CRANE", WordishRules.draft(full))
        assertEquals("CRAN", WordishRules.draft(WordishRules.delete(puzzle, full)))
        assertEquals(emptyMap<String, String>(), WordishRules.input(puzzle, emptyMap(), '1'))
    }
    @Test fun repeatedGuessDoesNotConsumeAnotherAttempt() {
        val first = WordishRules.submit(puzzle, enter("CRANE"))
        val repeat = WordishRules.submit(puzzle, enter("CRANE", first))
        assertEquals(listOf("CRANE"), WordishRules.guesses(repeat))
        assertTrue(repeat["message"]!!.contains("already guessed"))
    }
    @Test fun solvedGameIsReadOnly() {
        val win = WordishRules.submit(puzzle, enter("APPLE"))
        assertEquals(PlayStatus.COMPLETED, WordishRules.result(puzzle, win).status)
        assertEquals(1, WordishRules.result(puzzle, win).score)
        assertEquals(win, WordishRules.input(puzzle, win, 'A'))
        assertEquals(win, WordishRules.delete(puzzle, win))
        assertEquals(win, WordishRules.submit(puzzle, win))
    }
    @Test fun sixthWrongGuessFailsAndRevealsAnswer() {
        var state = emptyMap<String, String>()
        listOf("CRANE", "PLANT", "BRICK", "SHORE", "LIGHT", "MUSIC").forEach { state = WordishRules.submit(puzzle, enter(it, state)) }
        assertEquals(PlayStatus.FAILED, WordishRules.result(puzzle, state).status)
        assertTrue(WordishRules.result(puzzle, state).summary.contains("APPLE"))
        assertEquals(state, WordishRules.input(puzzle, state, 'A'))
    }
    @Test fun sixthCorrectGuessWinsRatherThanFails() {
        var state = mapOf("guesses" to "CRANE,PLANT,BRICK,SHORE,LIGHT", "draft" to "APPLE")
        state = WordishRules.submit(puzzle, state)
        assertEquals(PlayStatus.COMPLETED, WordishRules.result(puzzle, state).status)
    }
    @Test fun mapRestoreRetainsGuessesAndUnsubmittedDraft() {
        val state = enter("AP", WordishRules.submit(puzzle, enter("CRANE")))
        val restored = state.entries.associate { it.key to it.value }
        assertEquals(listOf("CRANE"), WordishRules.guesses(restored))
        assertEquals("AP", WordishRules.draft(restored))
        assertEquals(WordishRules.result(puzzle, state), WordishRules.result(puzzle, restored))
    }
}
