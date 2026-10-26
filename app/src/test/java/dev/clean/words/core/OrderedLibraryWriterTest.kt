package dev.clean.words.core

import org.junit.Assert.*
import org.junit.Test
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.util.concurrent.Executor
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class OrderedLibraryWriterTest {
    @get:Rule val temporary = TemporaryFolder()
    private val inline = Executor { it.run() }

    @Test fun rapidSavesAreOrderedAndLatestCallbackMatchesLatestSnapshot() {
        val versions = CopyOnWriteArrayList<Long>()
        val writtenData = CopyOnWriteArrayList<String>()
        val writer = OrderedLibraryWriter({ writtenData.add(it.attempts.single().state.getValue("value")) }, inline)
        try {
            repeat(80) { index ->
                val attempt = Attempt("id", "fixture", PuzzleMode.EXTRA, null,
                    PuzzleRecord("p", title = "fixture", data = emptyMap()), state = mapOf("value" to index.toString()),
                    startedAt = "2026-10-25T00:00:00Z", updatedAt = "2026-10-25T00:00:00Z")
                writer.submit(index.toLong(), Library(listOf(attempt)), onSaved = versions::add,
                    onFailed = { throw AssertionError(it) })
            }
            assertTrue(writer.drain(5000))
            assertEquals((0L..79L).toList(), versions.toList())
            assertEquals((0..79).map(Int::toString), writtenData.toList())
        } finally { writer.close() }
    }
    @Test fun failureStopsQueuedWritesAndRetryWritesOnlyLatestSnapshot() {
        val writes = CopyOnWriteArrayList<Int>()
        var failures = 0
        val saved = CopyOnWriteArrayList<Long>()
        val writer = OrderedLibraryWriter({ library ->
            writes.add(library.attempts.size)
            if (writes.size == 1) throw java.io.IOException("disk full")
        }, inline)
        try {
            writer.submit(1, Library(), onSaved = saved::add, onFailed = { failures++ })
            writer.submit(2, Library(), onSaved = saved::add, onFailed = { failures++ })
            writer.submit(3, Library(), onSaved = saved::add, onFailed = { failures++ })
            assertTrue(writer.drain(5000))
            assertEquals(1, writes.size); assertEquals(1, failures); assertTrue(saved.isEmpty())
            writer.submit(4, Library(), retry = true, onSaved = saved::add, onFailed = { failures++ })
            assertTrue(writer.drain(5000))
            assertEquals(2, writes.size); assertEquals(listOf(4L), saved.toList())
        } finally { writer.close() }
    }
    @Test fun submitDoesNotBlockTheInputThreadOnDisk() {
        val entered = CountDownLatch(1); val release = CountDownLatch(1)
        val writer = OrderedLibraryWriter({ entered.countDown(); release.await(3, TimeUnit.SECONDS) }, inline)
        try {
            writer.submit(1, Library(), onSaved = {}, onFailed = {})
            assertTrue(entered.await(2, TimeUnit.SECONDS))
            assertFalse(writer.drain(10))
            release.countDown()
            assertTrue(writer.drain(2000))
        } finally { release.countDown(); writer.close() }
    }

    @Test fun cancelledStartBeforeWriteLeavesNoHistoryAndDuringWriteRestoresBaseline() {
        val before = Library()
        val next = Library(listOf(Attempt("new", "wordish", PuzzleMode.EXTRA, null,
            PuzzleRecord("p", title = "fixture", data = emptyMap()),
            startedAt = "2026-10-25T00:00:00Z", updatedAt = "2026-10-25T00:00:00Z")))
        val writes = CopyOnWriteArrayList<Library>(); var active = true; var discarded = 0
        val writer = OrderedLibraryWriter({ value -> writes += value; active = false }, inline)
        try {
            writer.submitStart(before, next, { false }, onSaved = { fail("cancelled start opened") },
                onDiscarded = { discarded++ }, onFailed = { throw AssertionError(it) })
            assertTrue(writer.drain(2000)); assertTrue(writes.isEmpty())
            writer.submitStart(before, next, { active }, onSaved = { fail("cancelled during write") },
                onDiscarded = { discarded++ }, onFailed = { throw AssertionError(it) })
            assertTrue(writer.drain(2000))
            assertEquals(listOf(next, before, before), writes.toList()); assertEquals(2, discarded)
        } finally { writer.close() }
    }

    @Test fun failedStartNeverOpensAndRetryRestoresBaseline() {
        val writes = CopyOnWriteArrayList<Library>(); var failures = 0; var saved = false
        val writer = OrderedLibraryWriter({ library ->
            writes += library
            if (writes.size == 1) throw java.io.IOException("disk full")
        }, inline)
        try {
            writer.submitStart(Library(), Library(), { true }, onSaved = { fail("failed start opened") },
                onDiscarded = { fail("not cancelled") }, onFailed = { failures++ })
            assertTrue(writer.drain(2000)); assertEquals(1, failures)
            writer.submitStart(Library(), Library(), { true }, onSaved = { fail("queue is blocked") },
                onDiscarded = {}, onFailed = { failures++ })
            assertTrue(writer.drain(2000)); assertEquals(2, failures); assertEquals(1, writes.size)
            writer.submit(3, Library(), retry = true, onSaved = { saved = true }, onFailed = { throw AssertionError(it) })
            assertTrue(writer.drain(2000)); assertTrue(saved)
        } finally { writer.close() }
    }

    @Test fun cancellationAfterWriteBeforeUiCallbackRollsBackAndReportsRollbackFailure() {
        val ui = java.util.concurrent.ConcurrentLinkedQueue<Runnable>()
        val writes = CopyOnWriteArrayList<Library>(); var active = true; var failures = 0; var discard = 0
        val before = Library()
        val next = Library(listOf(Attempt("new", "wordish", PuzzleMode.EXTRA, null,
            PuzzleRecord("p", title = "fixture", data = emptyMap()),
            startedAt = "2026-10-25T00:00:00Z", updatedAt = "2026-10-25T00:00:00Z")))
        var failRollback = true
        val writer = OrderedLibraryWriter({ value ->
            writes += value
            if (writes.size == 2 && failRollback) throw java.io.IOException("rollback disk full")
        }, Executor { ui.add(it) })
        fun dispatch() { while (true) (ui.poll() ?: break).run() }
        try {
            writer.submitStart(before, next, { active }, onSaved = { fail("late cancelled start opened") },
                onDiscarded = { discard++ }, onFailed = { failures++ })
            assertTrue(writer.drain(2000)); assertEquals(listOf(next), writes.toList())
            active = false; dispatch() // simulates Back after durable write, before UI acknowledgement
            assertTrue(writer.drain(2000)); dispatch()
            assertEquals(listOf(next, before), writes.toList()); assertEquals(1, failures); assertEquals(0, discard)
            failRollback = false
            writer.submit(2, before, retry = true, onSaved = {}, onFailed = { fail(it.message) })
            assertTrue(writer.drain(2000)); dispatch(); assertEquals(before, writes.last())

            active = true
            writer.submitStart(before, next, { active }, onSaved = { fail("late cancel") },
                onDiscarded = { discard++ }, onFailed = { fail(it.message) })
            assertTrue(writer.drain(2000)); active = false; dispatch()
            assertTrue(writer.drain(2000)); dispatch(); assertEquals(1, discard)
            assertEquals(before, writes.last())
        } finally { writer.close() }
    }

    @Test fun retryOfFailedCancellationPurgesAbandonedAttemptFromRecoveryCopyToo() {
        val directory = temporary.newFolder(); val store = AtomicLibraryStore(directory)
        val before = Library()
        val next = Library(listOf(Attempt("abandoned", "wordish", PuzzleMode.EXTRA, null,
            PuzzleRecord("p", title = "fixture", data = emptyMap()),
            startedAt = "2026-10-25T00:00:00Z", updatedAt = "2026-10-25T00:00:00Z")))
        store.save(before)
        var active = true; var writes = 0; var failure = 0
        val writer = OrderedLibraryWriter({ value ->
            writes++
            if (writes == 2) throw java.io.IOException("first rollback blocked")
            store.save(value)
            if (value == next) active = false
        }, inline)
        try {
            writer.submitStart(before, next, { active }, onSaved = { fail("cancelled") },
                onDiscarded = { fail("rollback failed") }, onFailed = { failure++ })
            assertTrue(writer.drain(3000)); assertEquals(1, failure)
            assertEquals(next, store.load().library) // failure is visible; candidate must not be opened
            writer.submit(2, before, retry = true, retainOnlySnapshot = true, onSaved = {}, onFailed = { fail(it.message) })
            assertTrue(writer.drain(3000)); assertEquals(before, store.load().library)
            java.io.File(directory, "attempts.cw").writeText("simulate corruption after successful retry")
            assertEquals(before, store.load().library)
            assertEquals(before, LibraryCodec.decode(java.io.File(directory, "attempts.previous.cw").readBytes()))
        } finally { writer.close() }
    }
}
