package dev.clean.words.core

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.zip.CRC32
import java.time.Instant
import java.time.LocalDate

class UnsupportedSaveVersion(val version: Int) : IllegalArgumentException("Save version $version is not supported by this app; original files preserved")

/** Bounded, checksummed, versioned snapshots; independent of Android for real JVM tests. */
object LibraryCodec {
    private const val MAGIC = 0x43575244
    private const val VERSION = 1
    private const val MAX_FILE = 64 * 1024 * 1024
    private const val MAX_STRING = 2 * 1024 * 1024

    fun encode(library: Library): ByteArray {
        val body = ByteArrayOutputStream()
        DataOutputStream(body).use { out ->
            out.writeInt(library.attempts.size)
            library.attempts.forEach { a ->
                out.text(a.id); out.text(a.gameId); out.text(a.mode.name); out.text(a.puzzleDate ?: "")
                out.text(a.puzzle.id); out.writeInt(a.puzzle.revision); out.text(a.puzzle.title)
                out.map(a.puzzle.data); out.map(a.state)
                out.text(a.startedAt); out.text(a.updatedAt); out.text(a.completedAt ?: "")
                out.text(a.result.status.name); out.text(a.result.summary)
                out.writeBoolean(a.result.score != null); a.result.score?.let(out::writeInt)
                out.writeBoolean(a.result.maxScore != null); a.result.maxScore?.let(out::writeInt)
                out.writeBoolean(a.result.assisted)
            }
        }
        val bytes = body.toByteArray()
        require(bytes.size <= MAX_FILE) { "Save is too large; existing save preserved" }
        return ByteArrayOutputStream().also { envelope ->
            DataOutputStream(envelope).use { out ->
                out.writeInt(MAGIC); out.writeInt(VERSION); out.writeInt(bytes.size)
                out.writeLong(CRC32().apply { update(bytes) }.value); out.write(bytes)
            }
        }.toByteArray()
    }

    fun decode(bytes: ByteArray): Library {
        require(bytes.size <= MAX_FILE + 20) { "Save exceeds supported size" }
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            require(input.readInt() == MAGIC) { "Unrecognized save" }
            val version = input.readInt()
            if (version != VERSION) throw UnsupportedSaveVersion(version)
            val size = input.readInt()
            require(size in 4..MAX_FILE && size == input.available() - 8) { "Truncated save" }
            val crc = input.readLong()
            val body = ByteArray(size); input.readFully(body)
            require(CRC32().apply { update(body) }.value == crc) { "Save checksum failed" }
            DataInputStream(ByteArrayInputStream(body)).use { data ->
                val count = data.readInt(); require(count in 0..100000)
                val attempts = List(count) {
                    val id = data.text(); val gameId = data.text(); val mode = PuzzleMode.valueOf(data.text())
                    val date = data.text().ifEmpty { null }
                    val puzzle = PuzzleRecord(data.text(), data.readInt(), data.text(), data.map())
                    val state = data.map()
                    val start = data.text(); val update = data.text(); val end = data.text().ifEmpty { null }
                    val status = PlayStatus.valueOf(data.text()); val summary = data.text()
                    val score = if (data.readBoolean()) data.readInt() else null
                    val maxScore = if (data.readBoolean()) data.readInt() else null
                    val assisted = data.readBoolean()
                    require(id.isNotEmpty() && gameId.isNotEmpty() && puzzle.revision > 0)
                    if (mode == PuzzleMode.DAILY) { require(date != null); LocalDate.parse(date) }
                    else require(date == null)
                    Instant.parse(start); Instant.parse(update); end?.let(Instant::parse)
                    require((status == PlayStatus.IN_PROGRESS) == (end == null)) { "Invalid completion timestamp" }
                    Attempt(id, gameId, mode, date, puzzle, state, start, update, end,
                        GameResult(status, summary, score, maxScore, assisted))
                }
                require(data.available() == 0) { "Unexpected save data" }
                require(attempts.map { it.id }.distinct().size == attempts.size) { "Duplicate attempt IDs" }
                val dailyKeys = attempts.filter { it.mode == PuzzleMode.DAILY }.map { it.gameId to it.puzzleDate }
                require(dailyKeys.distinct().size == dailyKeys.size) { "Duplicate daily attempts" }
                return Library(attempts)
            }
        }
    }

    private fun DataOutputStream.text(value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        require(bytes.size <= MAX_STRING); writeInt(bytes.size); write(bytes)
    }
    private fun DataInputStream.text(): String {
        val size = readInt(); require(size in 0..MAX_STRING && size <= available())
        return ByteArray(size).also { readFully(it) }.toString(Charsets.UTF_8)
    }
    private fun DataOutputStream.map(map: Map<String, String>) {
        writeInt(map.size); map.toSortedMap().forEach { (key, value) -> text(key); text(value) }
    }
    private fun DataInputStream.map(): Map<String, String> {
        val size = readInt(); require(size in 0..10000)
        val result = linkedMapOf<String, String>()
        repeat(size) { require(result.put(text(), text()) == null) { "Duplicate field" } }
        return result
    }
}
