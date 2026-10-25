package dev.clean.words.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.time.Instant
import java.time.ZoneId
import androidx.lifecycle.ViewModel

/** Saves before returning from accepted UI edits; tiny S1 snapshots avoid pending background writes. */
class AppSession(directory: File) : ViewModel() {
    private val store = AtomicLibraryStore(directory)
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
        // Keep the user's latest edit in memory even if disk writing fails.
        library = next
        return try {
            store.save(next)
            pendingSave = false; error = null; true
        } catch (e: Exception) {
            pendingSave = true
            error = "This change is not saved: ${e.message}. Keep the app open and retry. Previous files are preserved."
            false
        }
    }

    fun retrySave() { if (pendingSave) commit(library) else reload() }

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
