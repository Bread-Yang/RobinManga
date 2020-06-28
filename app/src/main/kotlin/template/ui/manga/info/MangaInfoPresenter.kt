package template.ui.manga.info

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import template.data.cache.CoverCache
import template.data.database.DatabaseHelper
import template.data.database.models.Manga
import template.data.download.DownloadManager
import template.extensions.isNullOrDisposed
import template.source.Source
import template.ui.common.mvp.BasePresenter
import template.ui.manga.MangaController
import java.util.*
import javax.inject.Inject

class MangaInfoPresenter : BasePresenter<MangaInfoController>() {

    @Inject
    lateinit var db: DatabaseHelper

    @Inject
    lateinit var downloadManager: DownloadManager

    @Inject
    lateinit var coverCache: CoverCache

    lateinit var manga: Manga

    lateinit var source: Source

    lateinit var chapterCountSubject: BehaviorSubject<Float>

    lateinit var lastUpdateSubject: BehaviorSubject<Date>

    lateinit var mangaFavoriteSubject: PublishSubject<Boolean>

    /**
     * Disposable to send the manga to the view.
     */
    private var viewMangaDispoable: Disposable? = null

    /**
     * Disposable to update the manga from the source.
     */
    private var fetchMangaDisposable: Disposable? = null

    fun init(parentcontroller: MangaController) {
        this.manga = parentcontroller.manga!!
        this.source = parentcontroller.source!!
        this.chapterCountSubject = parentcontroller.chapterCountSubject
        this.lastUpdateSubject = parentcontroller.lastUpdateSubject
        this.mangaFavoriteSubject = parentcontroller.mangaFavoriteSubject

        sendMangaToView()

        // Update chapter count
        chapterCountSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeLatestCache(null, MangaInfoController::setChapterCount)

        // Update favorite status
        mangaFavoriteSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    setFavorite(it)
                }
                .apply {
                    add(this)
                }

        // Update last update date
        lastUpdateSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeLatestCache(null, MangaInfoController::setLastUpdateDate)
    }

    /**
     * Sends the active manga to the view.
     */
    fun sendMangaToView() {
        viewMangaDispoable?.let {
            remove(viewMangaDispoable)
        }
        viewMangaDispoable = Observable.just(manga)
                .subscribeLatestCache { view, manga ->
                    view.onNextManga(manga, source)
                }
    }

    /**
     * Fetch manga information from source.
     */
    fun fetchMangaFromSource() {
        if (!fetchMangaDisposable.isNullOrDisposed()) return

        fetchMangaDisposable = Observable
                .defer {
                    source.fetchMangaDetails(manga)
                }
                .map {
                    manga.copyFrom(it)
                    manga.initialized = true
                    db.insertManga(manga).executeAsBlocking()
                    manga
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    sendMangaToView()
                }
                .subscribeFirst({ view, _ ->
                    view.onFetchMangaError()
                }, { view, manga ->
                    view.onFetchMangaDone()
                })
    }

    /**
     * Update favorite status of manga. (removes / adds) manga (to / from) library.
     *
     * @return the new status of the manga.
     */
    fun toggleFavorite(): Boolean {
        manga.favorite = !manga.favorite
        if (!manga.favorite) {
            coverCache.deleteFromCache(manga.thumbnail_url)
        }
        db.insertManga(manga).executeAsBlocking()
        sendMangaToView()
        return manga.favorite
    }

    private fun setFavorite(favorite: Boolean) {
        if (manga.favorite == favorite) {
            return
        }
        toggleFavorite()
    }


}