package template.ui.download

import android.view.View
import kotlinx.android.synthetic.main.download_item.view.*
import template.data.download.model.Download
import template.ui.base.holder.BaseViewHolder

/**
 * Class used to hold the data of a download.
 * All the elements from the layout file "download_item" are available in this class.
 *
 * @param view the inflated view for this holder.
 * @constructor creates a new download holder.
 */
class DownloadHolder(private val view: View) : BaseViewHolder(view) {

    private lateinit var download: Download

    /**
     * Method called from [DownloadAdapter.onBindViewHolder]. It updates the data for this
     * holder with the given download.
     *
     * @param download the download to bind.
     */
    fun onSetValues(download: Download) {
        this.download = download

        // Update the chapter name.
        view.tvChapterTitle.text = download.chapter.name

        // Update the manga title
        view.tvMangaTitle.text = download.manga.title

        // Update the progress bar and the number of donwloaded pages
        val pages = download.pages
        if (pages == null) {
            view.pbDownloadProgress.progress = 0
            view.pbDownloadProgress.max = 1
            view.tvDownloadProgress.text = ""
        } else {
            view.pbDownloadProgress.max = pages.size * 100
            notifyProgress()
            notifyDownloadedPages()
        }
    }

    /**
     * Updates the progress bar of the download.
     */
    fun notifyProgress() {
        val pages = download.pages ?: return
        if (view.pbDownloadProgress.max == 1) {
            view.pbDownloadProgress.max = pages.size * 100
        }
        view.pbDownloadProgress.progress = download.totalProgress
    }

    /**
     * Updates the text field of the number of downloaded pages.
     */
    fun notifyDownloadedPages() {
        val pages = download.pages ?: return
        view.tvDownloadProgress.text = "${download.downloadedImages}/${pages.size}"
    }
}
