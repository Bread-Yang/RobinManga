package template.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.os.EnvironmentCompat
import java.io.File
import java.io.InputStream
import java.net.URLConnection

object DiskUtil {

    fun isImage(name: String, openStream: (() -> InputStream)? = null): Boolean {
        val contentType = try {
            URLConnection.guessContentTypeFromName(name)
        } catch (e: Exception) {
            null
        } ?: openStream?.let { findImageMime(it) }

        return contentType?.startsWith("image/") ?: false
    }

    fun findImageMime(openStream: () -> InputStream): String? {
        try {
            openStream().buffered().use {
                val bytes = ByteArray(8)
                it.mark(bytes.size)
                val length = it.read(bytes, 0, bytes.size)
                it.reset()
                if (length == -1)
                    return null
                if (bytes[0] == 'G'.toByte() && bytes[1] == 'I'.toByte() && bytes[2] == 'F'.toByte() && bytes[3] == '8'.toByte()) {
                    return "image/gif"
                } else if (bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte()
                        && bytes[3] == 0x47.toByte() && bytes[4] == 0x0D.toByte() && bytes[5] == 0x0A.toByte()
                        && bytes[6] == 0x1A.toByte() && bytes[7] == 0x0A.toByte()) {
                    return "image/png"
                } else if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()) {
                    return "image/jpeg"
                } else if (bytes[0] == 'W'.toByte() && bytes[1] == 'E'.toByte() && bytes[2] == 'B'.toByte() && bytes[3] == 'P'.toByte()) {
                    return "image/webp"
                }
            }
        } catch (e: Exception) {
        }
        return null
    }

    fun hashKeyForDisk(key: String): String {
        return Hash.md5(key)
    }

    fun getDirectorySize(f: File): Long {
        var size: Long = 0
        if (f.isDirectory) {
            for (file in f.listFiles()) {
                size += getDirectorySize(file)
            }
        } else {
            size = f.length()
        }
        return size
    }

    /**
     * Returns the root folders of all the available external storages.
     */
    fun getExternalStorages(context: Context): Collection<File> {
        val directories = mutableSetOf<File>()
        // 通过Context.getExternalFilesDir()方法可以获取到 SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
        directories += ContextCompat.getExternalFilesDirs(context, null)
                .filterNotNull()
                .mapNotNull {
                    val file = File(it.absolutePath.substringBefore("/Android/"))
                    val state = EnvironmentCompat.getStorageState(file)
                    if (state == Environment.MEDIA_MOUNTED || state == Environment.MEDIA_MOUNTED_READ_ONLY) {
                        file
                    } else {
                        null
                    }
                }

        if (Build.VERSION.SDK_INT < 21) {
            val extStorages = System.getenv("SECONDARY_STORAGE")
            if (extStorages != null) {
                directories += extStorages.split(":").map(::File)
            }
        }

        return directories
    }

    /**
     * Scans the given file so that it can be shown in gallery apps, for example.
     */
    fun scanMedia(context: Context, file: File) {
        scanMedia(context, Uri.fromFile(file))
    }

    /**
     * Scans the given file so that it can be shown in gallery apps, for example.
     */
    fun scanMedia(context: Context, uri: Uri) {
        val action = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Intent.ACTION_MEDIA_MOUNTED
        } else {
            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
        }
        val mediaScanIntent = Intent(action)
        mediaScanIntent.data = uri
        context.sendBroadcast(mediaScanIntent)
    }

    /**
     * Mutate the given filename to make it valid for a FAT filesystem,
     * replacing any invalid characters with "_". This method doesn't allow hidden files (starting
     * with a dot), but you can manually add it later.
     */
    fun buildValidFilename(origName: String): String {
        val name = origName.trim('.', ' ')
        if (name.isNullOrEmpty()) {
            return "(invalid)"
        }
        val sb = StringBuilder(name.length)
        name.forEach { c ->
            if (isValidFatFilenameChar(c)) {
                sb.append(c)
            } else {
                sb.append('_')
            }
        }
        // Even though vfat allows 255 UCS-2 chars, we might eventually write to
        // ext4 through a FUSE layer, so use that limit minus 15 reserved characters.
        return sb.toString().take(240)
    }

    /**
     * Returns true if the given character is a valid filename character, false otherwise.
     */
    private fun isValidFatFilenameChar(c: Char): Boolean {
        if (0x00.toChar() <= c && c <= 0x1f.toChar()) {
            return false
        }
        return when (c) {
            '"', '*', '/', ':', '<', '>', '?', '\\', '|', 0x7f.toChar() -> false
            else -> true
        }
    }
}