package io.github.artcraftandroid.pdfcraft.engine

import java.io.File
import org.json.JSONArray
import org.json.JSONObject

/** A document opened before, as Home's Recent list shows it. */
data class RecentFile(
    /** Where it was opened from: a `content://` or `file://` URI. */
    val uri: String,
    val name: String,
    /** When it was last opened, ms since the epoch. */
    val opened: Long,
    val pages: Int,
    val size: Long,
    /** The page the reader was on (0-based), to reopen there. */
    val lastPage: Int = 0,
    val starred: Boolean = false,
    /** It was left in Reading Mode, to reopen that way (the reference remembers it for each file). */
    val reading: Boolean = false,
)

/**
 * The recent documents, newest first, kept in a JSON file in the app's private files (at most
 * [MAX] of them). Reads and writes are small and synchronous; callers stay off the main thread.
 */
class RecentFiles(private val file: File) {
    private val lock = Any()

    fun list(): List<RecentFile> = synchronized(lock) { read() }

    fun touch(entry: RecentFile) = synchronized(lock) {
        val old = read().firstOrNull { it.uri == entry.uri }
        val merged = entry.copy(starred = old?.starred ?: entry.starred, reading = old?.reading ?: entry.reading)
        write(listOf(merged) + read().filter { it.uri != entry.uri })
    }

    fun update(uri: String, f: (RecentFile) -> RecentFile) = synchronized(lock) {
        write(read().map { if (it.uri == uri) f(it) else it })
    }

    fun remove(uris: Set<String>) = synchronized(lock) { write(read().filter { it.uri !in uris }) }

    fun clear() = synchronized(lock) { write(emptyList()) }

    private fun read(): List<RecentFile> {
        if (!file.isFile) return emptyList()
        return runCatching {
            val a = JSONArray(file.readText())
            (0 until a.length()).mapNotNull { i ->
                val o = a.optJSONObject(i) ?: return@mapNotNull null
                RecentFile(
                    uri = o.optString("uri").ifEmpty { return@mapNotNull null },
                    name = o.optString("name", "Document.pdf"),
                    opened = o.optLong("opened"),
                    pages = o.optInt("pages"),
                    size = o.optLong("size"),
                    lastPage = o.optInt("lastPage"),
                    starred = o.optBoolean("starred"),
                    reading = o.optBoolean("reading"),
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun write(list: List<RecentFile>) {
        val a = JSONArray()
        for (r in list.take(MAX)) {
            a.put(
                JSONObject().put("uri", r.uri).put("name", r.name).put("opened", r.opened).put("pages", r.pages)
                    .put("size", r.size).put("lastPage", r.lastPage).put("starred", r.starred).put("reading", r.reading),
            )
        }
        val tmp = File(file.parentFile, file.name + ".tmp")
        tmp.writeText(a.toString())
        if (!tmp.renameTo(file)) {
            file.delete()
            tmp.renameTo(file)
        }
    }

    companion object {
        const val MAX = 100
    }
}
