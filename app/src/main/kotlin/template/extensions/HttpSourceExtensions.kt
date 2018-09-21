package template.extensions

import android.net.Uri
import io.reactivex.Observable
import template.App
import template.data.cache.ChapterCache
import template.data.database.models.Chapter
import template.source.model.Page
import template.source.online.HttpSource

/**
 * Chapter cache.
 */
private val chapterCache: ChapterCache by lazy {
    App.app.lazyChapterCache.get()
}

/**
 * Returns an observable with the page list for a chapter. It tries to return the page list from
 * the local cache, otherwise fallbacks to network.
 *
 * @param chapter the chapter whose page list has to be fetched.
 */
fun HttpSource.fetchPageListFromCacheThenNet(chapter: Chapter): Observable<List<Page>> {
    return chapterCache
            .getPageListFromCache(chapter)
            .onErrorResumeNext(fetchPageList(chapter))
}

/**
 * Returns an observable of the page with the downloaded image.
 *
 * @param page the page whose source image has to be downloaded.
 */
fun HttpSource.fetchImageFromCacheThenNet(page: Page): Observable<Page> {
    return if (page.imageUrl.isNullOrEmpty()) {
        getImageUrl(page)
                .flatMap {
                    getCachedImage(it)
                }
    } else {
        getCachedImage(page)
    }
}

fun HttpSource.getImageUrl(page: Page): Observable<Page> {
    page.status = Page.LOAD_PAGE
    return fetchImageUrl(page)
            .doOnError {
                page.status = Page.ERROR
            }
            .onErrorReturn {
                null
            }
            .doOnNext {
                page.imageUrl = it
            }
            .map {
                page
            }
}

/**
 * Returns an observable of the page that gets the image from the chapter or fallbacks to
 * network and copies it to the cache calling [cacheImage].
 *
 * @param page the page.
 */
fun HttpSource.getCachedImage(page: Page): Observable<Page> {
    val imageUrl = page.imageUrl ?: return Observable.just(page)

    return Observable.just(page)
            .flatMap {
                if (!chapterCache.isImageInCache(imageUrl)) {
                    cacheImage(page)
                } else {
                    Observable.just(page)
                }
            }
            .doOnNext {
                page.uri = Uri.fromFile(chapterCache.getImageFile(imageUrl))
                page.status = Page.READY
            }
            .doOnError {
                page.status = Page.ERROR
            }
            .onErrorReturn {
                page
            }
}

/**
 * Returns an observable of the page that downloads the image to [ChapterCache].
 *
 * @param page the page.
 */
private fun HttpSource.cacheImage(page: Page): Observable<Page> {
    page.status = Page.DOWNLOAD_IMAGE
    return fetchImage(page)
            .doOnNext {
                chapterCache.putImageToCache(page.imageUrl!!, it)
            }
            .map {
                page
            }
}

fun HttpSource.fetchAllImageUrlsFromPageList(pages: List<Page>): Observable<Page> {
//    return Observable
//            .fromIterable(pages)
//            .filter {
//                !it.imageUrl.isNullOrEmpty()
//            }
//            .mergeWith {
//                fetchRemainingImageUrlsFromPageList(pages)
//            }
    return Observable
            .fromIterable(pages)
            .flatMap {
                if (it.imageUrl.isNullOrEmpty()) {
                    return@flatMap getImageUrl(it)
                } else {
                    return@flatMap Observable.just(it)
                }
            }
}

fun HttpSource.fetchRemainingImageUrlsFromPageList(pages: List<Page>): Observable<Page> {
    return Observable
            .fromIterable(pages)
            .filter {
                it.imageUrl.isNullOrEmpty()
            }
            .concatMap {
                getImageUrl(it)
            }
}

