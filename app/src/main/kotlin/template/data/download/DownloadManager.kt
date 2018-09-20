package template.data.download

import android.content.Context

/**
 * This class is used to manage chapter downloads in the application. It must be instantiated once
 * and retrieved through dependency injection. You can use this class to queue new chapters or query
 * downloaded chapters.
 *
 * @param context the application context.
 */
class DownloadManager(context: Context) {

    /**
     * Downloads path provider, used to retrieve the folders where the chapters are or should be stored.
     */
    private val pathProvider = DownloadPathProvider(context)

    /**
     * Cache of downloaded chapters.
     */
    private val cache = DownloadCache(context, pathProvider)


}