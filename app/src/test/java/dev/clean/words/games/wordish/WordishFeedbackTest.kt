package dev.clean.words.games.wordish

import org.junit.Assert.*
import org.junit.Test

class WordishFeedbackTest {
    private val puzzle = WordishGame.puzzles.first() // APPLE

    @Test fun keyboardEvidenceUsesStrongestFeedbackAcrossRepeatedLettersAndGuesses() {
        val state = mapOf("guesses" to "ALLEY,AMPLE,CRANE")
        val colors = WordishRules.keyboardFeedback(puzzle, state)
        assertEquals(LetterFeedback.CORRECT, colors['A'])
        assertEquals(LetterFeedback.CORRECT, colors['L'])
        assertEquals(LetterFeedback.CORRECT, colors['E'])
        assertEquals(LetterFeedback.CORRECT, colors['P'])
        assertEquals(LetterFeedback.ABSENT, colors['Y'])
        assertEquals(LetterFeedback.ABSENT, colors['C'])
        assertNull(colors['Z'])
    }
    @Test fun coloredLettersCanStillBeEnteredAndClearOnlyRemovesDraft() {
        val played = mapOf("guesses" to "CRANE", "draft" to "ALL", "message" to "try")
        val cleared = WordishRules.clear(puzzle, played)
        assertEquals(listOf("CRANE"), WordishRules.guesses(cleared))
        assertEquals("", WordishRules.draft(cleared))
        val gray = WordishRules.input(puzzle, cleared, 'C')
        assertEquals("C", WordishRules.draft(gray))
        val greenOrYellow = WordishRules.input(puzzle, gray, 'A')
        assertEquals("CA", WordishRules.draft(greenOrYellow))
    }
}
