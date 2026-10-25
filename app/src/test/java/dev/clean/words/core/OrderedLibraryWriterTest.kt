package dev.clean.words.core

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.Executor
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class OrderedLibraryWriterTest {
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
}
