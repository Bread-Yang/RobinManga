package template.utils

import java.io.File
import java.io.InputStream

object DiskUtil {

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