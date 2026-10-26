package dev.clean.words.core

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** One IO lane: callbacks never reorder durable writes, and failure stops later writes until retry. */
class OrderedLibraryWriter(
    private val write: (Library) -> Unit,
    private val callbacks: Executor,
) {
    private val worker = Executors.newSingleThreadExecutor { task -> Thread(task, "clean-words-save") }
    private var failed = false // accessed only on worker

    fun submit(version: Long, library: Library, retry: Boolean = false, retainOnlySnapshot: Boolean = false,
        onSaved: (Long) -> Unit, onFailed: (Exception) -> Unit) {
        worker.execute {
            if (retry) failed = false
            if (!failed) {
                try {
                    write(library)
                    if (retainOnlySnapshot) write(library)
                    callbacks.execute { onSaved(version) }
                }
                catch (e: Exception) { failed = true; callbacks.execute { onFailed(e) } }
            }
        }
    }

    /** A new puzzle is invisible until durable. Cancelled writes restore the prior full snapshot. */
    fun submitStart(before: Library, next: Library, active: () -> Boolean,
        onSaved: () -> Unit, onDiscarded: () -> Unit, onFailed: (Exception) -> Unit) {
        worker.execute {
            try {
                check(!failed) { "Earlier changes need saving before a new puzzle can start" }
                if (!active()) { callbacks.execute { onDiscarded() }; return@execute }
                write(next)
                if (!active()) {
                    write(before)
                    write(before) // remove the cancelled start from the retained last-good copy too
                    callbacks.execute { onDiscarded() }
                } else callbacks.execute {
                    if (active()) onSaved()
                    else worker.execute {
                        try { write(before); write(before); callbacks.execute { onDiscarded() } }
                        catch (e: Exception) { failed = true; callbacks.execute { onFailed(e) } }
                    }
                }
            } catch (e: Exception) {
                failed = true
                callbacks.execute { onFailed(e) }
            }
        }
    }

    fun drain(timeoutMillis: Long = 1500): Boolean = runCatching {
        worker.submit {}.get(timeoutMillis, TimeUnit.MILLISECONDS)
        true
    }.getOrDefault(false)

    fun close() { worker.shutdown() } // queued writes finish; never drop accepted edits
}
