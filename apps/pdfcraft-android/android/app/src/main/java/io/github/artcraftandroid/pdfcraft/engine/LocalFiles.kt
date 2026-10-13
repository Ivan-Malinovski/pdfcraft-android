package io.github.artcraftandroid.pdfcraft.engine

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import io.github.artcraftandroid.pdfcraft.core.fileThumbnail
import java.io.File
import java.security.MessageDigest

/** A PDF on the phone: where it is, its name, folder, size and when it was last changed. */
data class LocalPdf(val uri: Uri, val path: String?, val name: String, val folder: String, val size: Long, val modified: Long)

/**
 * The PDFs on the phone's storage, found through the media store (every PDF with all-files
 * access, only the app's own without it), newest first.
 */
object LocalFiles {
    /** Whether the app may see every file (Android 11+: all-files access; earlier: storage permission). */
    fun hasAccess(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager()
        else context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED

    fun scan(context: Context): List<LocalPdf> {
        val out = ArrayList<LocalPdf>()
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.RELATIVE_PATH,
            @Suppress("DEPRECATION") MediaStore.Files.FileColumns.DATA,
        )
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?"
        runCatching {
            context.contentResolver.query(collection, projection, selection, arrayOf("application/pdf", "%.pdf"), "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC")?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val relCol = c.getColumnIndex(MediaStore.Files.FileColumns.RELATIVE_PATH)
                @Suppress("DEPRECATION") val dataCol = c.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                while (c.moveToNext()) {
                    val name = c.getString(nameCol) ?: continue
                    if (name.startsWith(".")) continue
                    val path = if (dataCol >= 0) c.getString(dataCol) else null
                    // a file the app reads by path when it may (so saving writes in place)
                    val uri = if (path != null && hasAccess(context)) Uri.fromFile(File(path)) else ContentUris.withAppendedId(collection, c.getLong(idCol))
                    val rel = if (relCol >= 0) c.getString(relCol) else null
                    val folder = rel?.trimEnd('/')?.substringAfterLast('/') ?: path?.let { File(it).parentFile?.name } ?: ""
                    out.add(LocalPdf(uri, path, name, folder, c.getLong(sizeCol), c.getLong(dateCol) * 1000))
                }
            }
        }
        return out
    }
}

/**
 * Small pictures of documents' first pages for file lists, rendered by the engine and kept on
 * disk (keyed by the file and its last change), so a list scrolls without re-rendering.
 */
class Thumbnails(context: Context) {
    private val dir = File(context.cacheDir, "thumbs").apply { mkdirs() }
    private val memory = object : android.util.LruCache<String, Bitmap>(12 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }

    /** The thumbnail of the PDF at [path] (null when it can't be drawn: protected, damaged). Blocking. */
    fun of(path: String, maxSide: Int = 160): Bitmap? {
        val f = File(path)
        if (!f.isFile) return null
        val key = sha("$path|${f.lastModified()}|${f.length()}|$maxSide")
        memory.get(key)?.let { return it }
        val cached = File(dir, "$key.png")
        if (cached.isFile) {
            BitmapFactory.decodeFile(cached.absolutePath)?.let {
                memory.put(key, it)
                return it
            }
        }
        val bmp = runCatching { fileThumbnail(path, maxSide.toUInt()).toBitmap() }.getOrNull() ?: return null
        runCatching { cached.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 90, it) } }
        memory.put(key, bmp)
        return bmp
    }

    private fun sha(s: String) = MessageDigest.getInstance("SHA-1").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
}
