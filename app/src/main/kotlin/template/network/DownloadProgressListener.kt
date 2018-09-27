package template.network

/**
 * Created by Robin Yeung on 8/22/18.
 */
interface DownloadProgressListener {
    fun updateProgress(bytesRead: Long, contentLength: Long, done: Boolean)
}