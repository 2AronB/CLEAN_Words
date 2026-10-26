package dev.clean.words

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.clean.words.core.*
import dev.clean.words.games.bee.BeeGame
import dev.clean.words.games.connections.ConnectionsGame
import dev.clean.words.games.crossword.CrosswordGame
import dev.clean.words.games.strands.StrandsGame
import dev.clean.words.games.wordish.WordishGame
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object GameRegistry {
    val games: List<GameModule> = listOf(WordishGame, ConnectionsGame, BeeGame, StrandsGame, CrosswordGame)
    fun get(id: String) = games.first { it.id == id }
    fun contentErrors() = games.flatMap { game ->
        val counts = if (game.puzzles.size < 10) listOf("${game.title} needs ten samples") else emptyList()
        counts + game.puzzles.flatMap { puzzle -> game.validate(puzzle).map { "${game.id}/${puzzle.id}: $it" } }
    }
}

class MainActivity : ComponentActivity() {
    private var activeSession: AppSession? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContext = applicationContext
        val session = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AppSession(File(appContext.filesDir, "play-library")) { name ->
                    appContext.assets.open(name).bufferedReader().use { it.readText() }
                } as T
        })[AppSession::class.java]
        activeSession = session
        setContent {
            CleanTheme { CleanWords(session) { name -> assets.open(name).bufferedReader().use { it.readText() } } }
        }
    }
    override fun onStop() {
        activeSession?.cancelStart()
        activeSession?.flush()
        super.onStop()
    }
}

@Composable
private fun CleanTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) darkColorScheme(
        primary = Color(0xFFC9B9FF), secondary = Color(0xFFFFB8CC), tertiary = Color(0xFFB5E9DE),
        background = Color(0xFF191824), surface = Color(0xFF211F30),
        primaryContainer = Color(0xFF514065), secondaryContainer = Color(0xFF633D50), tertiaryContainer = Color(0xFF315E54),
    ) else lightColorScheme(
        primary = Color(0xFF63468F), secondary = Color(0xFF9D4764), tertiary = Color(0xFF2D6B60),
        background = Color(0xFFFFF8F4), surface = Color(0xFFFFF8F4),
        primaryContainer = Color(0xFFDFCEFF), secondaryContainer = Color(0xFFFFCBDC), tertiaryContainer = Color(0xFFB5E9DE),
    )
    MaterialTheme(colorScheme = colors, content = content)
}

@Composable
private fun CleanWords(session: AppSession, readAsset: (String) -> String) {
    var page by rememberSaveable { mutableStateOf("home") }
    var gameId by rememberSaveable { mutableStateOf(WordishGame.id) }
    var attemptId by rememberSaveable { mutableStateOf("") }
    var help by rememberSaveable { mutableStateOf(false) }
    var clock by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) {
        while (true) { kotlinx.coroutines.delay(1000); clock = Instant.now() }
    }
    val today = AttemptRules.puzzleDate(clock, ZoneId.systemDefault())
    LaunchedEffect(today) { session.checkBoundary(today) }
    LaunchedEffect(page, gameId) { if (page != "hub") session.cancelStart() }
    LaunchedEffect(session.openedAttemptId) {
        session.openedAttemptId?.let { id ->
            session.library.attempts.firstOrNull { it.id == id }?.let {
                gameId = it.gameId; attemptId = id; page = "play"
            }
            session.consumeOpenedAttempt()
        }
    }
    val contentErrors = remember { GameRegistry.contentErrors() }
    val game = GameRegistry.get(gameId)
    val compact = page == "play" && gameId == "crossword"
    val pageScroll = remember(page, gameId, attemptId) { ScrollState(0) }
    val entrance = remember(page, gameId, attemptId) { Animatable(0f) }
    LaunchedEffect(entrance) { entrance.animateTo(1f, tween(230)) }
    fun back() {
        session.cancelStart()
        page = when (page) { "play", "extras" -> "hub"; "hub" -> "home"; else -> "home" }
    }
    fun navigate(destination: String) { session.cancelStart(); page = destination }
    fun showAttempt(attempt: Attempt) {
        session.cancelStart(); gameId = attempt.gameId; attemptId = attempt.id; page = "play"
    }
    BackHandler(enabled = page != "home") { back() }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.safeDrawingPadding().imePadding().fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = if (compact) 12.dp else 20.dp, vertical = if (compact) 2.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                if (page != "home") TextButton(onClick = { back() }) { Text("‹ Back") }
                Column(Modifier.weight(1f)) {
                    if (!compact) Text("CLEAN WORDS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold)
                    Text(when (page) { "home" -> "A little word time."; "hub", "play", "extras" -> game.title
                        "calendar" -> "Your puzzle calendar"; else -> "Credits & sources" },
                        style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall)
                }
                if (page == "play" || page == "hub") TextButton(onClick = { help = true }) { Text("Help") }
            }
            HorizontalDivider()
            Column(Modifier.weight(1f).graphicsLayer {
                alpha = entrance.value
                translationY = (1f - entrance.value) * 10.dp.toPx()
            }.verticalScroll(pageScroll).padding(if (compact) 12.dp else 20.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 14.dp)) {
                session.notice?.let { message ->
                    Note(message) { TextButton(onClick = session::dismissNotice) { Text("Dismiss") } }
                }
                if (session.error != null) {
                    Note(session.error!!) { Button(onClick = session::retrySave) { Text("Retry save / load") } }
                }
                session.launching?.let { progress ->
                    Note(progress.phase) {
                        if (progress.phase != "Cancelling start…") TextButton(onClick = session::cancelStart) { Text("Cancel start") }
                    }
                }
                if (contentErrors.isNotEmpty()) {
                    Text("Content checks failed. Play is disabled; saved data has not been erased.", color = MaterialTheme.colorScheme.error)
                    contentErrors.forEach { Text(it) }
                } else if (!session.blocked) {
                    when (page) {
                        "home" -> {
                            Text(today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")), style = MaterialTheme.typography.titleMedium)
                            Text("Five small challenges. No noise. Daily puzzles change at 6 a.m. on your phone.")
                            GameRegistry.games.forEachIndexed { index, module ->
                                val saved = AttemptRules.daily(session.library, module, today)
                                val cardColor = listOf(Color(0xFFC7EAC6), Color(0xFFFFE398), Color(0xFFFFD2A1),
                                    Color(0xFFBDE7F4), Color(0xFFDBC8FF))[index]
                                Card(onClick = { gameId = module.id; navigate("hub") }, modifier = Modifier.fillMaxWidth().animateContentSize(tween(220)),
                                    colors = CardDefaults.cardColors(containerColor = cardColor, contentColor = Color(0xFF2D2938))) {
                                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(listOf("W", "••", "B", "S", "+")[index],
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = Color(0xFF594168), modifier = Modifier.width(48.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(module.title, style = MaterialTheme.typography.titleLarge)
                                            Text(module.description, style = MaterialTheme.typography.bodyMedium)
                                            Text(saved?.let { AttemptPresentation.inspect(it).label } ?: "Today's puzzle is ready", style = MaterialTheme.typography.labelMedium)
                                            saved?.let { OpeningSummary(AttemptPresentation.inspect(it)) }
                                        }
                                    }
                                }
                            }
                            OutlinedButton(onClick = { navigate("calendar") }, modifier = Modifier.fillMaxWidth()
                                .semantics { contentDescription = "Calendar & history" }) { Text("Calendar & history") }
                            TextButton(onClick = { navigate("credits") }) { Text("Credits & sources") }
                            Text("Development edition · tWordle, Speling Bee, Connektions and Crossita generate locally from finite banks. Stranded uses 10 starters. Difficulty is provisional.",
                                style = MaterialTheme.typography.bodySmall)
                        }
                        "hub" -> {
                            val current = AttemptRules.daily(session.library, game, today)
                            Text("Daily · ${today.format(DateTimeFormatter.ofPattern("MMM d"))}", style = MaterialTheme.typography.headlineMedium)
                            Text(if (OfflinePuzzleGeneration.isGenerated(gameId))
                                "Generated offline from a finite word bank. Familiarity and daily challenge still need player review. Saved dailies keep their original puzzle."
                                else "Starter collection · 10 puzzles. Repeats are possible; daily challenge has not been calibrated.")
                            if (gameId == "bee") Text("New puzzles: your first ten distinct accepted words set an opening score against this board’s best ten. Keep playing afterward for the word goal and more points. Score fairness is provisional.")
                            if (gameId == "crossword") Text("New puzzles use varied 7×7 layouts. Today’s saved 5×5 stays unchanged; choose Another puzzle to try the larger grids now. Clue quality and challenge still need your feedback.")
                            if (gameId == "connections") Text("Boards include tempting partial connections across the four solution groups. Categories and ambiguity still need player review; this is a small provisional relationship bank.")
                            val currentProgress = current?.let { AttemptPresentation.inspect(it) }
                            currentProgress?.let { OpeningSummary(it) }
                            if (gameId == "bee" && current != null && currentProgress?.opening == null && currentProgress?.errors?.isEmpty() == true)
                                Text("Today's saved Bee keeps its original rules. Choose Another puzzle to try the new ten-word opening now.", style = MaterialTheme.typography.bodyMedium)
                            session.launchError?.let { message ->
                                Note(message) { TextButton(onClick = { session.retryGeneration(game) },
                                    enabled = !session.pendingSave && session.launching == null) { Text("Retry local generation") } }
                            }
                            Button(onClick = {
                                session.open(game, PuzzleMode.DAILY)
                            }, enabled = !session.pendingSave && session.launching == null, modifier = Modifier.fillMaxWidth()) {
                                Text(if (current == null) "Play today's puzzle" else if (currentProgress?.needsResume == true)
                                    "Continue today's puzzle" else "View today's result")
                            }
                            val previous = AttemptRules.previousUnfinished(session.library, gameId, today)
                            previous?.let { old ->
                                OutlinedButton(onClick = { showAttempt(old) }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Finish previous puzzle? · ${old.puzzleDate}")
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("Stay a little longer", style = MaterialTheme.typography.titleLarge)
                            Text(if (gameId == "crossword")
                                "Generate another 7×7 fill. Recent fills are excluded, the previous puzzle’s exact clues are avoided, and older answers and clues are deprioritized. Common answers may recur; the pilot is finite."
                                else if (OfflinePuzzleGeneration.isGenerated(gameId))
                                "Generate another independent puzzle, excluding content from your latest 100 starts for this game. The bank is finite; exhaustion is reported."
                                else "Start another independent starter puzzle. The ten-puzzle collection cycles; repeats keep separate progress.")
                            Button(onClick = { session.open(game, PuzzleMode.EXTRA) },
                                enabled = !session.pendingSave && session.launching == null, modifier = Modifier.fillMaxWidth()) { Text("Another puzzle") }
                            OutlinedButton(onClick = { navigate("extras") }, modifier = Modifier.fillMaxWidth()) { Text("Additional-play history") }
                            TextButton(onClick = { navigate("calendar") }) { Text("Open calendar & history") }
                        }
                        "play" -> {
                            val attempt = session.library.attempts.firstOrNull { it.id == attemptId }
                            if (attempt == null) Text("This attempt is unavailable. Return to the game list; saved files were not changed.")
                            else {
                                val progress = remember(attempt) { AttemptPresentation.inspect(attempt) }
                                val savedErrors = remember(attempt.puzzle, attempt.state) {
                                    runCatching {
                                        game.validate(attempt.puzzle) + progress.errors +
                                            if (runCatching { game.result(attempt.puzzle, attempt.state) }.isFailure)
                                                listOf("Saved game state is unsupported") else emptyList()
                                    }.getOrElse { listOf("Saved puzzle revision cannot be read") }
                                }
                                Text(if (attempt.mode == PuzzleMode.DAILY) "Daily · ${attempt.puzzleDate}" else "Additional puzzle",
                                    style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                Text(if (attempt.puzzle.data.containsKey("generator")) "Generated locally · provisional content quality"
                                    else "Starter puzzle · saved original content", style = MaterialTheme.typography.bodySmall)
                                if (attempt.mode == PuzzleMode.DAILY && attempt.puzzleDate != today.toString())
                                    Text("Finishing this puzzle keeps its original calendar date.", style = MaterialTheme.typography.bodySmall)
                                if (savedErrors.isEmpty()) key(attempt.id) { game.Play(attempt.puzzle, attempt.state,
                                    enabled = !session.pendingSave && session.launching == null && (attempt.result.status == PlayStatus.IN_PROGRESS || game.id == "bee"),
                                    onChange = { session.change(attempt.id, it, game) }) }
                                else Text("This saved puzzle cannot be opened: ${savedErrors.joinToString()}. Its files and progress are preserved.",
                                    color = MaterialTheme.colorScheme.error)
                                AnimatedVisibility(savedErrors.isEmpty() && (attempt.result.status != PlayStatus.IN_PROGRESS || progress.opening?.recorded == true),
                                    enter = fadeIn(tween(260)) + expandVertically(tween(300))) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        HorizontalDivider(Modifier.padding(top = 8.dp))
                                        Text(attempt.result.summary, style = MaterialTheme.typography.titleMedium)
                                        Text("${progress.label} · saved in history", color = MaterialTheme.colorScheme.primary)
                                        if (progress.opening != null) { OpeningSummary(progress) }
                                        if (game.id == "bee") {
                                            Text(when {
                                                progress.opening == null -> "You can keep finding words after reaching the goal. This saved puzzle keeps its original rules."
                                                !progress.opening.recorded -> "The word goal is reached. Continue to ten accepted words to record your opening score."
                                                else -> "Keep exploring. Your recorded opening score stays fixed; later words add to your word goal and total points."
                                            })
                                        }
                                        OutlinedButton(onClick = { navigate("hub") }) { Text("Back to daily & more") }
                                    }
                                }
                                Text(if (session.saving) "Saving locally…" else if (session.pendingSave) "Changes need saving — retry above" else "Saved on this device",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        "calendar" -> CalendarScreen(session.library, gameId, onGame = { gameId = it }, onOpen = ::showAttempt, today = today)
                        "extras" -> {
                            Text("Additional play", style = MaterialTheme.typography.titleLarge)
                            val extra = session.library.attempts.filter { it.gameId == gameId && it.mode == PuzzleMode.EXTRA }.reversed()
                            if (extra.isEmpty()) Text("No additional puzzles started yet. Your daily puzzle has separate progress.")
                            extra.forEach { a -> AttemptCard(a) { showAttempt(a) } }
                        }
                        "credits" -> CreditsScreen(readAsset)
                    }
                }
            }
        }
    }
    if (help) AlertDialog(onDismissRequest = { help = false }, title = { Text(game.title) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(helpFor(gameId))
            Text("Back preserves progress. Daily puzzles change at 6 a.m. local time. Started older dailies remain in the calendar. Extra play is independent.")
            Text(if (OfflinePuzzleGeneration.isGenerated(gameId))
                "New puzzles are generated offline from finite banks. Saved puzzles keep their complete content and rules. Word familiarity and difficulty are provisional; daily content may repeat, and additional play excludes the latest 100 starts."
                else "Ten starter puzzles for this game; the collection repeats. On-demand generation for this game is not integrated yet.")
        }
    }, confirmButton = { TextButton(onClick = { help = false }) { Text("Got it") } })
}

@Composable
private fun Note(message: String, content: @Composable () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) { Text(message); content() }
    }
}

private fun PlayStatus.label() = when (this) {
    PlayStatus.IN_PROGRESS -> "In progress"; PlayStatus.COMPLETED -> "Completed"; PlayStatus.FAILED -> "Finished · not solved"
}

@Composable
private fun AttemptCard(attempt: Attempt, onClick: () -> Unit) {
    val progress = remember(attempt) { AttemptPresentation.inspect(attempt) }
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(if (attempt.mode == PuzzleMode.DAILY) "Daily · ${attempt.puzzleDate}" else attempt.puzzle.title,
                style = MaterialTheme.typography.titleMedium)
            Text(progress.label)
            OpeningSummary(progress)
            if (progress.opening == null) Text(attempt.result.summary, style = MaterialTheme.typography.bodySmall)
            progress.errors.takeIf { it.isNotEmpty() }?.let { Text("Saved inputs preserved; open for recovery details.") }
            attempt.completedAt?.let { Text("${if (progress.opening != null) "Word goal reached" else "Finished"} ${it.take(10)} (UTC date)", style = MaterialTheme.typography.labelSmall) }
        }
    }
}

@Composable
private fun OpeningSummary(progress: AttemptProgress) {
    progress.openingLabel?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    progress.goalLabel?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
}

@Composable
private fun CalendarScreen(library: Library, gameId: String, onGame: (String) -> Unit, onOpen: (Attempt) -> Unit, today: LocalDate) {
    var monthText by rememberSaveable { mutableStateOf(YearMonth.from(today).toString()) }
    var selectedDate by rememberSaveable { mutableStateOf(today.toString()) }
    val month = YearMonth.parse(monthText)
    Text("Started daily puzzles", style = MaterialTheme.typography.titleLarge)
    Text("Choose a game and date. Unstarted dates are empty; additional play has its own history.")
    GameRegistry.games.chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { game -> FilterChip(selected = game.id == gameId, onClick = { onGame(game.id) }, label = { Text(game.title) }) }
        }
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { monthText = month.minusMonths(1).toString() }) { Text("‹") }
        Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")), Modifier.weight(1f), textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = { monthText = month.plusMonths(1).toString() }) { Text("›") }
    }
    val attempts = library.attempts.filter { it.gameId == gameId && it.mode == PuzzleMode.DAILY }
    Row(Modifier.fillMaxWidth()) { listOf("M", "T", "W", "T", "F", "S", "S").forEach {
        Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
    } }
    val offset = month.atDay(1).dayOfWeek.value - 1
    val cellCount = ((offset + month.lengthOfMonth() + 6) / 7) * 7
    (0 until cellCount).chunked(7).forEach { week ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            week.forEach { index ->
                val day = index - offset + 1
                if (day !in 1..month.lengthOfMonth()) Spacer(Modifier.weight(1f).height(60.dp))
                else {
                    val date = month.atDay(day).toString()
                    val saved = attempts.firstOrNull { it.puzzleDate == date }
                    val chosen = selectedDate == date
                    val mark = saved?.let { AttemptPresentation.inspect(it).calendarMark }.orEmpty()
                    Column(Modifier.weight(1f).heightIn(min = 60.dp)
                        .background(if (chosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .clickable { selectedDate = date }.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(day.toString()); Text(mark, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
    Text("… unfinished (including Bee opening)   ✓ completed   × not solved   ! recovery", style = MaterialTheme.typography.bodySmall)
    Text(selectedDate, style = MaterialTheme.typography.titleMedium)
    val selected = attempts.firstOrNull { it.puzzleDate == selectedDate }
    if (selected == null) Text("No daily puzzle was started for this game on this date.")
    else AttemptCard(selected) { onOpen(selected) }
}

@Composable
private fun CreditsScreen(readAsset: (String) -> String) {
    Text("Words with a history", style = MaterialTheme.typography.headlineMedium)
    Text("ENABLE · Enhanced North American Benchmark Lexicon", style = MaterialTheme.typography.titleMedium)
    Text("Public-domain word list compiled by Alan Beale with M. Cooper and contributors. Used for accepted tWordle guesses and Bee vocabulary. Game-specific filters and familiar goal lists are CLEAN Words adaptations.")
    Text("Source: github.com/dolph/dictionary · enable1.txt\nPublic-domain notice: github.com/BartMassey/wordlists · README-enable2k.txt\nRetrieved October 25, 2026. Exact source hashes and transformations are recorded in the project.")
    var expanded by rememberSaveable { mutableStateOf(false) }
    TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Hide source notice" else "Read bundled source notice") }
    if (expanded) Text(remember { runCatching { readAsset("dictionaries/ENABLE-NOTICE.txt") }.getOrDefault("Bundled notice unavailable. Please report this packaging error.") },
        style = MaterialTheme.typography.bodySmall)
    HorizontalDivider()
    Text("Original starter content", style = MaterialTheme.typography.titleMedium)
    Text("Ten puzzles per game were selected or authored for CLEAN Words during development. Category explanations, themes and crossword clues are original starter drafts; they are not copied NYT puzzle collections. Mechanical checks do not establish final fairness or difficulty.")
    Text("Generated word games", style = MaterialTheme.typography.titleMedium)
    Text("tWordle uses an original 1,387-answer AI editorial shortlist with the unchanged ENABLE guess policy. Speling Bee derives seven-letter sets from original AI editorial pangram candidates, then enumerates every accepted word from its bundled ENABLE index. Familiarity lists set an attainable word-count goal; all accepted words count equally. Both banks have zero human or frequency review and provisional difficulty. Sources and license notice are bundled; no first-run download.")
    Text("New Speling Bee puzzles record the first ten accepted words against the board’s best-ten points. The full ENABLE accepted set and existing familiarity input are retained, with no extra familiarity-ratio filter. Score fairness remains provisional. Older saved puzzles keep their original rules.")
    Text("Connektions composes boards offline from 20 original AI-authored categories and five decoy relationships, with two deliberate three-word near-matches and checks against competing solutions in the recorded relationship model. These are provisional drafts with zero human reviews; this model cannot capture every possible interpretation. Category variety is limited. No published puzzle archive or external relationship dataset was used; no first-run download. Existing saved boards keep their exact content.")
    Text("Crossita fills varied 7×7 layouts offline from an original AI-authored clue and answer pilot. Clues, dimensions and answers are saved with each puzzle; older 5×5 games keep their exact content. The finite pilot has no human fairness or difficulty review, and some answers recur. No published puzzle archive, external clue dataset or first-run download is used.")
    Text("Built with Kotlin, AndroidX, Jetpack Compose and Material 3. No ads, accounts, analytics or required internet.")
}

private fun helpFor(id: String): String = when (id) {
    "wordish" -> "Guess a five-letter word in six tries. ✓ means correct place, ~ means elsewhere, × means absent. Repeated letters are counted only as often as they occur in the answer. Invalid words do not consume a guess."
    "connections" -> "Select four words with a shared connection. Find all four groups. Four distinct incorrect guesses end the puzzle. Repeating the same incorrect group does not cost another mistake."
    "bee" -> "Make words of four or more letters using the seven letters, always including the center. Letters may repeat. In new scored puzzles, your first ten distinct accepted words set an opening score: their points divided by this board’s best-ten points, times 100. Rejected and duplicate words do not use a slot. Before ten, the score is provisional; after ten, it stays fixed. Every accepted word also advances a separate word-count goal. You can reach that goal before recording the opening, or record the opening before reaching the goal. Keep playing after both. Bars show all available words by length. Older saved puzzles keep their original rules and have no retrofitted opening. Four letters earns 1 point; longer words their length; using all letters adds 7. Cross-board fairness is provisional; no extra familiarity-ratio filter is applied."
    "strands" -> "Find themed words along neighboring letters, including diagonals. A cell can be used once and found words cannot overlap. One spanning answer joins opposite edges. Tap letters, then Enter. Clear cancels the current path; Delete steps back. A route that blocks the remaining words is rejected without erasing prior play. Undo remains available for older routes."
    else -> "New puzzles use varied 7×7 grids; saved 5×5 puzzles keep their original size. Fill Across and Down answers. Crossing answers share a cell. Finishing a word advances to the next clue without checking it. Delete clears and moves backward. Use the built-in keyboard, or choose the phone keyboard. Check is optional and records assistance without deleting letters. A full but incorrect grid stays unfinished."
}
