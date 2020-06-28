package template.network

interface DownloadProgressListener {
    fun updateProgress(bytesRead: Long, contentLength: Long, done: Boolean)
}