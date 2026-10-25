package dev.clean.words.core

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

data class LibraryLoad(val library: Library?, val notice: String? = null, val blocked: Boolean = false)

/** App-private storage. Last-good copy is local recovery, not device-loss backup. */
class AtomicLibraryStore(private val directory: File) {
    private val current = File(directory, "attempts.cw")
    private val backup = File(directory, "attempts.previous.cw")
    private val pending = File(directory, "attempts.pending.cw")

    @Synchronized
    fun load(): LibraryLoad {
        if (!current.exists() && !backup.exists()) return LibraryLoad(Library())
        if (current.exists()) {
            val decoded = runCatching { LibraryCodec.decode(current.readBytes()) }
            if (decoded.exceptionOrNull() is UnsupportedSaveVersion)
                return LibraryLoad(null, decoded.exceptionOrNull()!!.message, true)
            decoded.getOrNull()?.let { return LibraryLoad(it) }
        }
        if (backup.exists()) runCatching { LibraryCodec.decode(backup.readBytes()) }.getOrNull()?.let {
            return LibraryLoad(it, "Recovered the previous local save. The newest move may be missing. The damaged file is preserved.")
        }
        return LibraryLoad(null, "Saved play could not be read. Files have been preserved. Retry or ask for recovery help; nothing was reset.", true)
    }

    @Synchronized
    fun save(library: Library) {
        val bytes = LibraryCodec.encode(library)
        check(directory.exists() || directory.mkdirs()) { "Cannot create save folder" }
        // Fully write and sync a replacement before touching the active file.
        FileOutputStream(pending).use { it.write(bytes); it.fd.sync() }
        LibraryCodec.decode(pending.readBytes())
        if (current.exists()) {
            val decoded = runCatching { LibraryCodec.decode(current.readBytes()) }
            if (decoded.exceptionOrNull() is UnsupportedSaveVersion) throw decoded.exceptionOrNull()!!
            val valid = decoded.isSuccess
            if (valid) {
                val backupPending = File(directory, "attempts.previous.pending.cw")
                FileOutputStream(backupPending).use { it.write(current.readBytes()); it.fd.sync() }
                Files.move(backupPending.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } else {
                val damaged = File(directory, "attempts.damaged-${System.currentTimeMillis()}.cw")
                Files.copy(current.toPath(), damaged.toPath())
            }
        }
        // Same directory atomic rename; fail visibly if the filesystem cannot do it.
        Files.move(pending.toPath(), current.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
    }
}
