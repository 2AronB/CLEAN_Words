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

/** UI changes are immediate; versioned snapshots write in order off the input thread. */
class AppSession(directory: File) : ViewModel() {
    private val store = AtomicLibraryStore(directory)
    private val main = Handler(Looper.getMainLooper())
    private val writer = OrderedLibraryWriter(store::save, Executor { main.post(it) })
    private var revision = 0L
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
        if (pendingSave) return
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
        writer.submit(version, next, retry, onSaved = { savedVersion ->
            if (savedVersion == revision) { saving = false; pendingSave = false; error = null }
        }, onFailed = { failure ->
            saving = false; pendingSave = true
            error = "Latest changes are not saved: ${failure.message}. Keep the app open and retry. Previous files are preserved."
        })
    }

    fun retrySave() { if (pendingSave) enqueue(library, retry = true) else reload() }
    fun flush() = writer.drain()
    override fun onCleared() { writer.close() }

    fun open(game: GameModule, mode: PuzzleMode, now: Instant = Instant.now(), zone: ZoneId = ZoneId.systemDefault()): String? {
        if (blocked || pendingSave) return null
        val attempt = AttemptRules.create(library, game, mode, now, zone)
        return if (library.attempts.any { it.id == attempt.id } || commit(AttemptRules.upsert(library, attempt))) attempt.id else null
    }

    fun change(id: String, state: Map<String, String>, game: GameModule) {
        if (blocked || pendingSave) return
        val old = library.attempts.firstOrNull { it.id == id } ?: return
        if (old.gameId != game.id) return
        if (old.result.status != PlayStatus.IN_PROGRESS && game.id != "bee") return
        commit(AttemptRules.upsert(library, AttemptRules.update(old, state, game, Instant.now())))
    }
}
