package dev.clean.words.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.time.Instant
import java.time.ZoneId
import androidx.lifecycle.ViewModel
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.UUID
import java.time.LocalDate

data class LaunchProgress(val gameId: String, val mode: PuzzleMode, val date: LocalDate, val phase: String)

/** UI changes are immediate; versioned snapshots write in order off the input thread. */
class AppSession(directory: File, readAsset: (String) -> String) : ViewModel() {
    private val store = AtomicLibraryStore(directory)
    private val main = Handler(Looper.getMainLooper())
    private val writer = OrderedLibraryWriter(store::save, Executor { main.post(it) })
    private val generationWorker = Executors.newSingleThreadExecutor { task -> Thread(task, "clean-words-generate") }
    private val engine = OfflinePuzzleGeneration(readAsset)
    private val coordinator = GenerationCoordinator(generationWorker, Executor { main.post(it) }, engine)
    private class Start(val request: GenerationRequest, val cancelled: AtomicBoolean = AtomicBoolean())
    private var start: Start? = null
    private var lastLaunch: Pair<String, PuzzleMode>? = null
    var launching by mutableStateOf<LaunchProgress?>(null)
        private set
    var launchError by mutableStateOf<String?>(null)
        private set
    var openedAttemptId by mutableStateOf<String?>(null)
        private set
    private var revision = 0L
    private var cleared = false
    private var cleanupStartSave = false
    var saving by mutableStateOf(false)
        private set
    var library by mutableStateOf(Library())
        private set
    var notice by mutableStateOf<String?>(null)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var blocked by mutableStateOf(false)
        private set
    var pendingSave by mutableStateOf(false)
        private set

    init { reload() }

    fun reload() {
        if (pendingSave || start != null) return
        val result = runCatching { store.load() }.getOrElse {
            LibraryLoad(null, "Cannot read saved play: ${it.message}. Files were not reset.", true)
        }
        result.library?.let { library = it }
        notice = result.notice
        blocked = result.blocked
        error = if (result.blocked) result.notice else null
    }

    fun dismissNotice() { notice = null }

    private fun commit(next: Library): Boolean {
        library = next
        enqueue(next)
        return true
    }

    private fun enqueue(next: Library, retry: Boolean = false) {
        val version = ++revision
        saving = true
        writer.submit(version, next, retry, retainOnlySnapshot = retry && cleanupStartSave, onSaved = { savedVersion ->
            if (savedVersion == revision) { saving = false; pendingSave = false; error = null; cleanupStartSave = false }
        }, onFailed = { failure ->
            saving = false; pendingSave = true
            error = "Latest changes are not saved: ${failure.message}. Keep the app open and retry. Previous files are preserved."
        })
    }

    fun retrySave() { if (pendingSave) enqueue(library, retry = true) else reload() }
    fun flush() = writer.drain()
    override fun onCleared() {
        cancelStart(); generationWorker.shutdown(); cleared = true
        if (start == null) writer.close() // a staged start finishes its cancellation/rollback first
    }

    fun consumeOpenedAttempt() { openedAttemptId = null }
    fun retryGeneration(game: GameModule) {
        lastLaunch?.takeIf { it.first == game.id }?.let { open(game, it.second) }
    }

    fun cancelStart() {
        launchError = null
        openedAttemptId = null
        coordinator.cancel()
        val current = start ?: return
        current.cancelled.set(true)
        if (launching?.phase == "Generating locally…") { start = null; launching = null }
        else launching = launching?.copy(phase = "Cancelling start…")
    }

    fun checkBoundary(today: LocalDate) {
        if (start?.request?.let { it.mode == PuzzleMode.DAILY && it.date != today } == true) cancelStart()
    }

    /** Existing snapshots open immediately; new content is generated and durably saved before navigation. */
    fun open(game: GameModule, mode: PuzzleMode, now: Instant = Instant.now(), zone: ZoneId = ZoneId.systemDefault()) {
        if (blocked || pendingSave || start != null) return
        val date = AttemptRules.puzzleDate(now, zone)
        if (mode == PuzzleMode.DAILY) AttemptRules.daily(library, game, date)?.let {
            openedAttemptId = it.id; return
        }
        val policy = engine.policy(game.id)
        val id = UUID.randomUUID().toString()
        val seed = if (mode == PuzzleMode.DAILY) GenerationSeeds.daily(game.id, policy, date)
            else GenerationSeeds.extra(game.id, policy, id)
        val recent = if (mode == PuzzleMode.DAILY) emptySet() else library.attempts.asReversed()
            .filter { it.gameId == game.id }.take(100).mapNotNull { engine.signature(game.id, it.puzzle) }.toSet()
        val request = GenerationRequest(game.id, mode, date, seed, id, policy, recent,
            library.attempts.count { it.gameId == game.id && it.mode == PuzzleMode.EXTRA })
        lastLaunch = game.id to mode
        val ticket = Start(request)
        start = ticket
        launchError = null
        launching = LaunchProgress(game.id, mode, date, "Generating locally…")
        fun active() = !ticket.cancelled.get() &&
            (mode != PuzzleMode.DAILY || AttemptRules.puzzleDate(Instant.now(), ZoneId.systemDefault()) == date)
        fun finish() {
            if (start === ticket) { start = null; launching = null; if (cleared) writer.close() }
        }
        fun fail(e: Exception) {
            finish(); saving = false; pendingSave = true; cleanupStartSave = true
            error = "New puzzle was not started: ${e.message}. Previous play is preserved. Retry save before starting again."
        }
        coordinator.start(request) { outcome ->
            if (start !== ticket) return@start
            if (!active()) { finish(); return@start }
            if (blocked || pendingSave) { finish(); return@start }
            when (outcome) {
                GenerationOutcome.Cancelled -> finish()
                is GenerationOutcome.Unavailable -> { finish(); launchError = outcome.message }
                is GenerationOutcome.Ready -> {
                    val attempt = runCatching { AttemptRules.createSnapshot(library, game, mode, outcome.puzzle,
                        Instant.now(), ZoneId.systemDefault(), id) }.getOrElse {
                        finish(); launchError = "Local generated content failed validation: ${it.message}. Saved play is preserved."
                        return@start
                    }
                    val before = library
                    val next = AttemptRules.upsert(before, attempt)
                    launching = launching?.copy(phase = "Saving puzzle locally…")
                    saving = true
                    ++revision // older queued-edit acknowledgements cannot finish this start
                    writer.submitStart(before, next, ::active, onSaved = {
                        // The writer checks the token/date again on this same UI callback lane.
                        library = next; saving = false; finish(); openedAttemptId = attempt.id
                    }, onDiscarded = { saving = false; finish() }, onFailed = ::fail)
                }
            }
        }
    }

    fun change(id: String, state: Map<String, String>, game: GameModule) {
        if (blocked || pendingSave || start != null) return
        val old = library.attempts.firstOrNull { it.id == id } ?: return
        if (old.gameId != game.id) return
        if (old.result.status != PlayStatus.IN_PROGRESS && game.id != "bee") return
        val next = runCatching { AttemptRules.update(old, state, game, Instant.now()) }.getOrElse {
            notice = "This edit could not be saved: ${it.message}. Previous inputs and results are preserved."
            return
        }
        commit(AttemptRules.upsert(library, next))
    }
}
