package template.utils

import android.content.ContentProvider
import android.content.ContentValues
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import junrar.Archive
import template.BuildConfig
import java.io.File
import java.io.IOException
import java.net.URLConnection
import java.util.concurrent.Executors

class RarContentProvider : ContentProvider() {

    private val pool by lazy { Executors.newCachedThreadPool() }

    companion object {
        const val PROVIDER = "${BuildConfig.APPLICATION_ID}.rar-provider"
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun getType(uri: Uri): String? {
        return URLConnection.guessContentTypeFromName(uri.toString())
    }

    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? {
        try {
            val pipe = ParcelFileDescriptor.createPipe()
            pool.execute {
                try {
                    val (rar, file) = uri.toString()
                            .substringAfter("content://$PROVIDER")
                            .split("!-/", limit = 2)

                    Archive(File(rar)).use { archive ->
                        val fileHeader = archive.fileHeaders.first { it.fileNameString == file }

                        ParcelFileDescriptor.AutoCloseOutputStream(pipe[1]).use { output ->
                            archive.extractFile(fileHeader, output)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
            return AssetFileDescriptor(pipe[0], 0, -1)
        } catch (e: IOException) {
            return null
        }
    }

    override fun query(p0: Uri?, p1: Array<out String>?, p2: String?, p3: Array<out String>?, p4: String?): Cursor? {
        return null
    }

    override fun insert(p0: Uri?, p1: ContentValues?): Uri {
        throw UnsupportedOperationException("not implemented")
    }

    override fun update(p0: Uri?, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        throw UnsupportedOperationException("not implemented")
    }

    override fun delete(p0: Uri?, p1: String?, p2: Array<out String>?): Int {
        throw UnsupportedOperationException("not implemented")
    }
}