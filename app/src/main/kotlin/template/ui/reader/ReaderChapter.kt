package template.ui.reader

import template.data.database.models.Chapter
import template.source.model.Page

/**
 * Chapter的代理类
 *
 * Created by Robin Yeung on 8/22/18.
 */
class ReaderChapter(c: Chapter) : Chapter by c {

    @Transient
    var pages: List<Page>? = null

    var isDownloaded: Boolean = false

    var requestedPage: Int = 0
}