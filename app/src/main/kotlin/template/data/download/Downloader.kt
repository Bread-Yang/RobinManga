package template.data.download

import android.content.Context
import template.data.download.model.DownloadQueue

/**
 * This class is the one in charge of downloading chapters.
 *
 * Its [queue] contains the list of chapters
 */
class Downloader(
        private val context: Context,
        private val pathProvider: DownloadPathProvider,
        private val cache: DownloadCache
) {

    /**
     * Store for persisting downloads across restarts.
     */
    private val store = DownloadStore(context)

    /**
     * Queue where active downloads are kept.
     */
    val queue = DownloadQueue(store)
}