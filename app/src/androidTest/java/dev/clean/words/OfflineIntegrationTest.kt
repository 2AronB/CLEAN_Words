package dev.clean.words

import android.content.pm.PackageManager
import android.os.SystemClock
import android.view.KeyEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.clean.words.core.*
import dev.clean.words.games.bee.*
import dev.clean.words.games.connections.*
import dev.clean.words.games.crossword.*
import dev.clean.words.games.strands.*
import dev.clean.words.games.wordish.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.Instant
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class OfflineIntegrationTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext

    @Test fun allFiftyPuzzlesCompleteThroughRulesAndPersistOnAndroid() {
        val store = AtomicLibraryStore(File(context.cacheDir, "s1-test-${UUID.randomUUID()}"))
        var library = Library()
        assertEquals(emptyList<String>(), GameRegistry.contentErrors())
        GameRegistry.games.forEach { game ->
            assertEquals(10, game.puzzles.size)
            game.puzzles.forEach { record ->
                val now = Instant.now().toString()
                var attempt = Attempt(UUID.randomUUID().toString(), game.id, PuzzleMode.EXTRA, null, record,
                    startedAt = now, updatedAt = now, result = game.result(record, emptyMap()))
                fun persist(state: Map<String,String>) {
                    attempt = AttemptRules.update(attempt, state, game, Instant.now())
                    library = AttemptRules.upsert(library, attempt)
                    store.save(library)
                    val restored = store.load()
                    assertFalse(restored.blocked)
                    assertEquals(library, restored.library)
                }
                when (game.id) {
                    "wordish" -> {
                        var state = emptyMap<String,String>()
                        WordishRules.answer(record).forEach { state = WordishRules.input(record, state, it); persist(state) }
                        persist(WordishRules.submit(record, state))
                    }
                    "connections" -> {
                        var state = emptyMap<String,String>()
                        repeat(4) { group ->
                            repeat(4) { tile -> state = ConnectionsRules.select(state, group * 4 + tile); persist(state) }
                            state = ConnectionsRules.submit(record, state); persist(state)
                        }
                    }
                    "bee" -> {
                        val board = BeePuzzle.from(record)
                        var state = BeeState()
                        board.goalWords.forEach { word -> state = BeeRules.submit(board, state, word); persist(state.toMap()) }
                    }
                    "strands" -> {
                        val board = StrandsPuzzle.from(record)
                        var state = StrandsState()
                        board.answers.forEach { answer ->
                            answer.path.forEach { cell -> state = StrandsRules.tap(board, state, cell); persist(state.toMap()) }
                            state = StrandsRules.submit(board, state); persist(state.toMap())
                        }
                    }
                    "crossword" -> {
                        val board = CrosswordRules.board(record)!!
                        var state = emptyMap<String,String>()
                        board.grid.forEachIndexed { index, letter -> if (letter != '#') {
                            state = CrosswordRules.enter(board, state, index, letter.toString()); persist(state)
                        } }
                    }
                }
                assertEquals("${game.id}/${record.id}", PlayStatus.COMPLETED, attempt.result.status)
                assertNotNull(attempt.completedAt)
                assertEquals(record, store.load().library!!.attempts.first { it.id == attempt.id }.puzzle)
            }
        }
        assertEquals(50, library.attempts.size)
        assertTrue(library.attempts.all { it.result.status == PlayStatus.COMPLETED })
    }

    @Test fun manifestRequiresNoInternetOrOtherRuntimePermissionsAndCreditsAreBundled() {
        @Suppress("DEPRECATION")
        val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
        assertTrue(info.requestedPermissions.orEmpty().none { it == "android.permission.INTERNET" })
        assertTrue(info.requestedPermissions.orEmpty().none { permission ->
            @Suppress("DEPRECATION")
            (context.packageManager.getPermissionInfo(permission, 0).protectionLevel and 0xf) == 1
        })
        assertTrue(context.assets.open("dictionaries/ENABLE-NOTICE.txt").bufferedReader().use { it.readText() }.contains("Public Domain"))
    }

    @Test fun allFiveBoardsNavigateRenderAndSurviveActivityRecreation() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // The cold API-37 emulator can show a System UI ANR unrelated to this app.
            if (has("System UI isn't responding")) click("Wait", exact = true)
            eventually("home") { has("A little word time.") }
            GameRegistry.games.forEach { game ->
                click(game.title)
                eventually("${game.title} hub") { has("Stay a little longer") }
                if (has("Play today's puzzle")) click("Play today's puzzle") else click("Continue today's puzzle")
                val boardMarker = when (game.id) {
                    "wordish" -> "correct place"; "connections" -> "Mistakes remaining";
                    "bee" -> "Center letter:"; "strands" -> "theme words"; else -> "Tap a square or clue"
                }
                eventually("${game.title} actual board") { has(boardMarker) }
                eventually("game help") { has("Help") }
                click("Help")
                eventually("help dialog") { has("Got it") }
                click("Got it")
                scenario.recreate()
                eventually("restored ${game.title}") { has(game.title) && has("Daily ·") }
                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                eventually("hub") { has("Another puzzle") }
                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                eventually("home") { has("A little word time.") }
            }
            // Touch input is real Compose accessibility interaction, then check app-private disk state.
            click("tWordle")
            eventually("tWordle hub") { has("Stay a little longer") }
            click("Another puzzle")
            eventually("tWordle board") { has("correct place") }
            click("A", exact = true)
            eventually("saved typed letter") {
                AtomicLibraryStore(File(context.filesDir, "play-library")).load().library?.attempts
                    ?.any { it.gameId == "wordish" && it.mode == PuzzleMode.EXTRA && it.state["draft"] == "A" } == true
            }
            scenario.recreate()
            eventually("restored draft") { has("A", exact = true) }
        }
    }

    @Test fun allThreeGeneratedGamesStartThroughUiAndResumeTheirFrozenContent() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            eventually("home") { has("A little word time.") }
            listOf(WordishGame, BeeGame, ConnectionsGame).forEach { game ->
                click(game.title)
                eventually("hub") { has("Stay a little longer") }
                val dailyLabel = when {
                    has("Play today's puzzle") -> "Play today's puzzle"
                    has("Continue today's puzzle") -> "Continue today's puzzle"
                    else -> "View today's result"
                }
                click(dailyLabel)
                eventually("daily board") { has(when(game.id) {"wordish" -> "correct place"; "bee" -> "Center letter:"; else -> "Mistakes remaining"}) }
                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                eventually("daily hub") { has("Another puzzle") }
                click("Another puzzle")
                eventually("generated board") { has("Generated locally · provisional content quality") }
                val disk = AtomicLibraryStore(File(context.filesDir, "play-library"))
                val attempt = disk.load().library!!.attempts.last { it.gameId == game.id && it.mode == PuzzleMode.EXTRA }
                assertNotNull(attempt.puzzle.data["generator"])
                val signature = OfflinePuzzleGeneration { "unused" }.signature(game.id, attempt.puzzle)
                assertNotNull(signature)
                if (game.id == "wordish") {
                    "APPLE".forEach { click(it.toString(), exact = true) }
                    eventually("five-letter input saved") { disk.load().library!!.attempts.first { it.id == attempt.id }.state["draft"] == "APPLE" }
                } else if (game.id == "bee") {
                    val word = BeePuzzle.from(attempt.puzzle).goalWords.minBy { it.length }
                    word.forEach { click(it.toString(), exact = true) }
                    click("Enter word", exact = true)
                    eventually("generated Bee word accepted and saved") {
                        word in BeeState.from(disk.load().library!!.attempts.first { it.id == attempt.id }.state).found
                    }
                } else {
                    val groups = ConnectionsRules.groups(attempt.puzzle)
                    val triple = attempt.puzzle.data.getValue("decoy.0.members").split('|')
                    val wrong = triple + groups.flatMap { it.words }.first { it !in triple }
                    repeat(2) {
                        wrong.forEachIndexed { index, word ->
                            click(word, exact = true)
                            eventually("selected wrong-set tile ${index + 1}") {
                                ConnectionsRules.selection(disk.load().library!!.attempts.first { it.id == attempt.id }.state).size == index + 1
                            }
                        }
                        click("Enter", exact = true)
                        eventually("same wrong set counts once") {
                            val state = disk.load().library!!.attempts.first { it.id == attempt.id }.state
                            ConnectionsRules.mistakes(state).size == 1 &&
                                ConnectionsRules.selection(state).size == 4
                        }
                        click("Clear", exact = true) // wrong submissions preserve selection under existing rules
                        eventually("selection cleared before next submission") {
                            ConnectionsRules.selection(disk.load().library!!.attempts.first { it.id == attempt.id }.state).isEmpty()
                        }
                    }
                    groups.first().words.forEach { click(it, exact = true) }
                    click("Enter", exact = true)
                    eventually("generated group solved and saved") {
                        ConnectionsRules.solved(disk.load().library!!.attempts.first { it.id == attempt.id }.state) == setOf(0)
                    }
                }
                val beforeRecreate = disk.load().library!!.attempts.first { it.id == attempt.id }
                scenario.recreate()
                eventually("restored generated content") {
                    if (has("Generated locally · provisional content quality")) true
                    else { scroll(backward = true); false }
                }
                instrumentation.uiAutomation.takeScreenshot()?.let { screenshot ->
                    java.io.FileOutputStream(File(context.cacheDir, "generation-${game.id}.png")).use {
                        assertTrue(screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it))
                    }
                    screenshot.recycle()
                }
                assertEquals(attempt.puzzle, disk.load().library!!.attempts.first { it.id == attempt.id }.puzzle)
                assertEquals(beforeRecreate.state, disk.load().library!!.attempts.first { it.id == attempt.id }.state)
                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                eventually("hub after generated play") { has("Another puzzle") }
                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                eventually("home after generated play") { has("A little word time.") }
            }
            click("Calendar & history")
            eventually("calendar") { has("Started daily puzzles") }
            click("tWordle", exact = true)
            val today = AttemptRules.puzzleDate(Instant.now(), java.time.ZoneId.systemDefault())
            click("Daily · $today", exact = true)
            eventually("frozen daily reopened from calendar") { has("correct place") && has("Daily · $today") }
            scenario.recreate()
            eventually("calendar-opened daily restored") { has("correct place") && has("Daily · $today") }
        }
    }

    @Test fun beeOpeningRecordsTenThroughUiAndKeepsScoreDuringExplorationAndHistory() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            eventually("home") { has("A little word time.") }
            click("Speling Bee")
            eventually("Bee hub") { has("Stay a little longer") }
            click("Another puzzle")
            eventually("active opening") { has("Opening 0/10") }
            val disk = AtomicLibraryStore(File(context.filesDir, "play-library"))
            val attempt = disk.load().library!!.attempts.last { it.gameId == "bee" && it.mode == PuzzleMode.EXTRA }
            val board = BeePuzzle.from(attempt.puzzle)
            assertTrue(board.opening); assertEquals("0", attempt.puzzle.data["minimumFamiliarRatioPermille"])
            fun saved() = disk.load().library!!.attempts.first { it.id == attempt.id }
            fun submit(word: String, expectedCount: Int) {
                enterBeeText(word)
                eventually("draft persisted") { BeeState.from(saved().state).entry == word }
                click("Enter word", exact = true)
                eventually("accepted count $expectedCount") {
                    val state = BeeState.from(saved().state)
                    state.acceptedOrder.orEmpty().size == expectedCount && (state.entry.isEmpty() || state.message.isNotEmpty())
                }
            }
            val ten = BeeOpening.bestTen(board)
            submit("ZZZZ", 0)
            submit(ten.first(), 1)
            submit(ten.first(), 1) // duplicate spends no additional opening slot
            ten.drop(1).take(7).forEachIndexed { index, word -> submit(word, index + 2) }
            assertEquals(ten.take(8), BeeState.from(saved().state).acceptedOrder)
            scenario.recreate()
            eventually("restored eight-word opening") { has("Opening 8/10") }
            ten.drop(8).forEachIndexed { index, word -> submit(word, index + 9) }
            val recorded = requireNotNull(AttemptPresentation.inspect(saved()).opening)
            assertTrue(recorded.recorded); assertEquals("100.00", recorded.percent.toPlainString())
            val more = board.validWords.filter { it !in ten }.sorted().take(2)
            more.forEachIndexed { index, word -> submit(word, index + 11) }
            val explored = saved()
            val after = requireNotNull(AttemptPresentation.inspect(explored).opening)
            assertEquals(recorded.words, after.words); assertEquals(recorded.percent, after.percent)
            assertTrue(after.allWordPoints > recorded.allWordPoints)
            assertEquals(ten + more, BeeState.from(explored.state).acceptedOrder)
            scenario.recreate()
            eventually("restored recorded opening") { has("Recorded opening: 100.00%") }
            instrumentation.uiAutomation.takeScreenshot()?.let { screenshot ->
                java.io.FileOutputStream(File(context.cacheDir, "bee-opening-recorded.png")).use {
                    assertTrue(screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it))
                }
                screenshot.recycle()
            }
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            eventually("hub") { has("Another puzzle") }
            click("Additional-play history")
            eventually("recorded opening in history") { has("Opening score: 100.00% · recorded") }
            click("Opening score: 100.00% · recorded", exact = true)
            eventually("same opening reopened") { has("Recorded opening: 100.00%") }
            assertEquals(explored.puzzle, saved().puzzle)
            assertEquals(ten + more, BeeState.from(saved().state).acceptedOrder)
        }
    }

    private fun enterBeeText(word: String) {
        fun editable(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            if (node == null) return null
            if (node.isVisibleToUser && node.isEditable) return node
            repeat(node.childCount) { editable(node.getChild(it))?.let { found -> return found } }
            return null
        }
        repeat(15) {
            val field = editable(instrumentation.uiAutomation.rootInActiveWindow)
            if (field != null) {
                val args = android.os.Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, word)
                }
                if (field.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) return
            }
            scroll(); SystemClock.sleep(100)
        }
        error("Bee input field unavailable")
    }

    private fun has(text: String, exact: Boolean = false) = find(text, exact) != null
    private fun find(text: String, exact: Boolean = false): AccessibilityNodeInfo? {
        fun visit(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            if (node == null) return null
            val value = node.text?.toString().orEmpty()
            if ((if (exact) value == text else value.contains(text)) && node.isVisibleToUser) return node
            repeat(node.childCount) { index -> visit(node.getChild(index))?.let { return it } }
            return null
        }
        return visit(instrumentation.uiAutomation.rootInActiveWindow)
    }
    private fun scroll(backward: Boolean = false): Boolean {
        fun visit(node: AccessibilityNodeInfo?): Boolean {
            if (node == null) return false
            if (node.isScrollable && node.performAction(if (backward) AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD else AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) return true
            repeat(node.childCount) { if (visit(node.getChild(it))) return true }
            return false
        }
        return visit(instrumentation.uiAutomation.rootInActiveWindow)
    }
    private fun click(text: String, exact: Boolean = false) {
        fun clickableMatch(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            if (node == null) return null
            val values = listOf(node.text?.toString().orEmpty(), node.contentDescription?.toString().orEmpty())
            if (node.isVisibleToUser && values.any { if (exact) it == text else it.contains(text) }) {
                var target: AccessibilityNodeInfo? = node
                while (target != null && !target.isClickable) target = target.parent
                if (target != null && target.isVisibleToUser && target.isEnabled) return target
            }
            repeat(node.childCount) { clickableMatch(node.getChild(it))?.let { found -> return found } }
            return null
        }
        repeat(10) {
            instrumentation.waitForIdleSync()
            if (android.os.Build.VERSION.SDK_INT >= 33) instrumentation.uiAutomation.clearCache()
            val node = clickableMatch(instrumentation.uiAutomation.rootInActiveWindow)
            if (node != null) {
                node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SHOW_ON_SCREEN.id)
                SystemClock.sleep(100)
                if (android.os.Build.VERSION.SDK_INT >= 33) instrumentation.uiAutomation.clearCache()
                val refreshed = clickableMatch(instrumentation.uiAutomation.rootInActiveWindow)
                if (refreshed?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) {
                    SystemClock.sleep(250); return
                }
                // Use the visible rendered target when a recreated card rejects its
                // accessibility action. Disabled targets never receive a forced tap.
                if (refreshed != null && refreshed.isVisibleToUser && refreshed.isEnabled) {
                    val bounds = android.graphics.Rect()
                    refreshed.getBoundsInScreen(bounds)
                    val display = context.resources.displayMetrics
                    if (bounds.intersect(0, 0, display.widthPixels, display.heightPixels) && !bounds.isEmpty) {
                        val now = SystemClock.uptimeMillis()
                        val down = android.view.MotionEvent.obtain(now, now, android.view.MotionEvent.ACTION_DOWN,
                            bounds.exactCenterX(), bounds.exactCenterY(), 0)
                        val up = android.view.MotionEvent.obtain(now, now + 80, android.view.MotionEvent.ACTION_UP,
                            bounds.exactCenterX(), bounds.exactCenterY(), 0)
                        try {
                            val sent = instrumentation.uiAutomation.injectInputEvent(down, true) &&
                                instrumentation.uiAutomation.injectInputEvent(up, true)
                            if (sent) { SystemClock.sleep(300); return }
                        } finally { down.recycle(); up.recycle() }
                    }
                }
            }
            scroll(backward = it >= 5); SystemClock.sleep(200)
        }
        fun texts(node: AccessibilityNodeInfo?): String {
            if (node == null) return "null root"
            return node.text?.toString().orEmpty() + " | " + (0 until node.childCount).joinToString(" | ") { texts(node.getChild(it)) }
        }
        error("Clickable text not found: $text; screen=${texts(instrumentation.uiAutomation.rootInActiveWindow)}")
    }
    private fun eventually(label: String, condition: () -> Boolean) {
        repeat(120) {
            if (has("System UI isn't responding")) click("Wait", exact = true)
            if (condition()) return
            SystemClock.sleep(100)
        }
        assertTrue("Timed out waiting for $label", condition())
    }
}
