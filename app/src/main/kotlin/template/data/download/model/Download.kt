package template.data.download.model

import io.reactivex.subjects.PublishSubject
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

    @Volatile
    @Transient
    var totalProgress: Int = 0

    @Volatile
    @Transient
    var downloadedImages: Int = 0

    @Transient
    private var statusSubject: PublishSubject<Download>? = null

    @Volatile
    @Transient
    var status: Int = 0
        set(status) {
            field = status
            statusSubject?.onNext(this)
        }

    fun setStatusSubject(subject: PublishSubject<Download>) {
        statusSubject = subject
    }
}