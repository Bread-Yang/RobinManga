package template.extensions

import template.App
import template.data.cache.ChapterCache

/**
 * Chapter cache.
 */
private val chapterCache: ChapterCache by lazy {
    App.app.lazyChapterCache.get()
}