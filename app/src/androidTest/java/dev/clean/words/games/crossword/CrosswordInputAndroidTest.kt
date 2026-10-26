package dev.clean.words.games.crossword

import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.clean.words.MainActivity
import dev.clean.words.core.AtomicLibraryStore
import dev.clean.words.core.Attempt
import dev.clean.words.core.PuzzleMode
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Run only on disposable test devices; uses the real Compose phone-input control. */
@RunWith(AndroidJUnit4::class)
class CrosswordInputAndroidTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext

    @Test fun rapidPhoneInputAndPasteKeepCharactersAndBackspaceDoesNotCheck() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            eventually { findText("A little word time.") != null }
            click("Crossita")
            eventually { findText("Stay a little longer") != null }
            click("Another puzzle")
            eventually { findText("Tap a square or clue") != null }
            val attempt = latestCrossword()!!
            val board = requireNotNull(CrosswordRules.board(attempt.puzzle))
            assertNotNull(attempt.puzzle.data["generator"])
            assertEquals(7, board.width)
            assertEquals(7, board.height)
            click("Use phone keyboard")
            eventually { editableNode() != null }
            // Real key events arrive without waiting for parent persistence/recomposition.
            instrumentation.sendStringSync("ABCDE")
            var expected = CrosswordInput.type(board, attempt.state, "ABCDE")
            eventually { matchesState(board, current(attempt.id)?.state, expected) }
            // One commit contains several additional characters, as a paste/IME burst does.
            val paste = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "ABCDEFGH")
            }
            assertTrue(editableNode()!!.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, paste))
            expected = CrosswordInput.type(board, expected, "FGH")
            eventually { matchesState(board, current(attempt.id)?.state, expected) }
            instrumentation.uiAutomation.takeScreenshot()?.let { screenshot ->
                java.io.FileOutputStream(File(context.cacheDir, "crossword-phone.png")).use {
                    assertTrue(screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it))
                }
                screenshot.recycle()
            }
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL)
            expected = CrosswordInput.delete(board, expected)
            eventually { matchesState(board, current(attempt.id)?.state, expected) }
            val saved = current(attempt.id)!!
            assertTrue(CrosswordRules.checked(saved.state).isEmpty())
            assertFalse(saved.result.assisted)
            assertNull(saved.state["checkedOnce"])

            // Recreation must reopen this exact frozen grid and retained cursor/letters.
            scenario.recreate()
            eventually { findText("Tap a square or clue") != null }
            assertEquals(saved.puzzle, current(attempt.id)!!.puzzle)
            assertEquals(saved.state, current(attempt.id)!!.state)
            eventually { findText("Use phone keyboard") != null }
            click("Q", exact = true)
            expected = CrosswordInput.type(board, expected, "Q")
            eventually { matchesState(board, current(attempt.id)?.state, expected) }
            assertNull(current(attempt.id)!!.state["checkedOnce"])
        }
    }

    private fun matchesState(board: CrosswordBoard, actual: Map<String, String>?, expected: Map<String, String>): Boolean =
        actual != null && CrosswordRules.cells(board, actual) == CrosswordRules.cells(board, expected) &&
            CrosswordInput.selected(board, actual) == CrosswordInput.selected(board, expected) &&
            CrosswordInput.direction(actual) == CrosswordInput.direction(expected)

    private fun latestCrossword(): Attempt? = AtomicLibraryStore(File(context.filesDir,"play-library"))
        .load().library?.attempts?.lastOrNull { it.gameId == "crossword" && it.mode == PuzzleMode.EXTRA }
    private fun current(id: String) = AtomicLibraryStore(File(context.filesDir,"play-library"))
        .load().library?.attempts?.firstOrNull { it.id == id }
    private fun visit(predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? {
        fun walk(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            if (node == null) return null
            if (node.isVisibleToUser && predicate(node)) return node
            repeat(node.childCount) { walk(node.getChild(it))?.let { found -> return found } }
            return null
        }
        return walk(instrumentation.uiAutomation.rootInActiveWindow)
    }
    private fun findText(text: String) = visit { it.text?.toString()?.contains(text) == true }
    private fun editableNode() = visit { it.isEditable }
    private fun click(text: String, exact: Boolean = false) {
        repeat(15) {
            val candidate = visit { node ->
                if (if (exact) node.text?.toString() != text else node.text?.toString()?.contains(text) != true) false
                else {
                    var at: AccessibilityNodeInfo? = node
                    while (at != null && !at.isClickable) at = at.parent
                    at != null
                }
            }
            if (candidate != null) {
                var target: AccessibilityNodeInfo? = candidate
                while (target != null && !target.isClickable) target = target.parent
                if (target?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) { SystemClock.sleep(200); return }
            }
            visit { it.isScrollable }?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            SystemClock.sleep(100)
        }
        error("Clickable text unavailable: $text")
    }
    private fun eventually(condition: () -> Boolean) {
        repeat(150) { if (condition()) return; SystemClock.sleep(100) }
        assertTrue("Timed out waiting for crossword input/navigation",condition())
    }
}
