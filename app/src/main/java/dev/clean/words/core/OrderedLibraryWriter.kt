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

    fun submit(version: Long, library: Library, retry: Boolean = false,
        onSaved: (Long) -> Unit, onFailed: (Exception) -> Unit) {
        worker.execute {
            if (retry) failed = false
            if (!failed) {
                try { write(library); callbacks.execute { onSaved(version) } }
                catch (e: Exception) { failed = true; callbacks.execute { onFailed(e) } }
            }
        }
    }

    fun drain(timeoutMillis: Long = 1500): Boolean = runCatching {
        worker.submit {}.get(timeoutMillis, TimeUnit.MILLISECONDS)
        true
    }.getOrDefault(false)

    fun close() { worker.shutdown() } // queued writes finish; never drop accepted edits
}
