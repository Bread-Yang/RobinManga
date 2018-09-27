package template.data.download.model

import io.reactivex.processors.PublishProcessor
import template.data.database.models.Chapter
import template.data.database.models.Manga
import template.source.model.Page
import template.source.online.HttpSource

/**
 * Created by Robin Yeung on 9/8/18.
 */
class Download(val source: HttpSource, val manga: Manga, val chapter: Chapter) {

    companion object {
        const val NOT_DOWNLOADED = 0
        const val QUEUE = 1
        const val DOWNLOADING = 2
        const val DOWNLOADED = 3
        const val ERROR = 4
    }

    var pages: List<Page>? = null

    /**
     * Download当前的下载进度，取值 <= pages.size * 100
     */
    @Volatile
    @Transient
    var totalProgress: Int = 0

    /**
     * 已下载的图片数，取值 <= pages.size
     */
    @Volatile
    @Transient
    var downloadedImages: Int = 0

    @Transient
    private var statusProcessor: PublishProcessor<Download>? = null

    @Volatile
    @Transient
    var status: Int = 0
        set(status) {
            field = status
            statusProcessor?.onNext(this)
        }

    fun setStatusProcessor(processor: PublishProcessor<Download>?) {
        statusProcessor = processor
    }
}