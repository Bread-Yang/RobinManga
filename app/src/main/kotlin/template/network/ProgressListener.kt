package template.network

/**
 * Created by Robin Yeung on 8/22/18.
 */
interface ProgressListener {
    fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}