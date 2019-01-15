package template.ui.library

import android.content.Context
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import template.data.cache.CoverCache
import template.data.database.DatabaseHelper
import template.data.database.models.Category
import template.data.database.models.Manga
import template.data.database.models.MangaCategory
import template.data.download.DownloadManager
import template.source.LocalSource
import template.source.SourceManager
import template.source.online.HttpSource
import template.ui.common.mvp.BasePresenter
import template.utils.preference.PreferencesHelper
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

/**
 * Class containing library information.
 */
private data class Library(val categories: List<Category>, val mangaMap: LibraryMap)

/**
 * typealias for the library manga, using the category as keys, and list of manga as values.
 */
private typealias LibraryMap = Map<Int, List<LibraryItem>>

/**
 * Presenter of [LibraryController].
 */
class LibraryPresenter : BasePresenter<LibraryController>() {

    @Inject
    lateinit var databaseHelper: DatabaseHelper

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    @Inject
    lateinit var coverCache: CoverCache

    @Inject
    lateinit var sourceManager: SourceManager

    @Inject
    lateinit var downloadManger: DownloadManager

    private val context: Context by lazy {
        view!!.applicationContext!!
    }

    /**
     * Categories of the library.
     */
    var categories: List<Category> = emptyList()
        private set

    /**
     * Subject used to apply the UI filters to the last emission of the library.
     */
    private val filterTriggerSubject = BehaviorSubject.createDefault(Unit)

    /**
     * Subject used to apply the UI update to the last emission of the library.
     */
    private val downloadTriggerSubject = BehaviorSubject.createDefault(Unit)

    /**
     * Subject used to apply the selected sorting method to the last emission of the library.
     */
    private val sortTriggerSubject = BehaviorSubject.createDefault(Unit)

    /**
     * Library disposable.
     */
    private var libraryDisposable: Disposable? = null

    /**
     * Requests the library to be filtered.
     */
    fun requestFilterUpdate() {
        filterTriggerSubject.onNext(Unit)
    }

    /**
     * Requests the library to have download badges added.
     */
    fun requestDownloadBadgesUpdate() {
        downloadTriggerSubject.onNext(Unit)
    }

    /**
     * Called when a manga is opened.
     */
    fun onOpenManga() {
        // Avoid further db updates for the library when it's not needed.
        libraryDisposable?.let {
            remove(it)
        }
    }

    /**
     * Returns the common categories for the given list of manga.
     *
     * @param mangas the list of manga.
     */
    fun getCommonCategories(mangas: List<Manga>): Collection<Category> {
        if (mangas.isEmpty()) return emptyList()
        return mangas.toSet()
                .map {
                    databaseHelper.getCategoriesForManga(it).executeAsBlocking()!!
                }
                .reduce { acc: Iterable<Category>, mutableList: MutableList<Category> ->
                    acc.intersect(mutableList)
                }
    }

    /**
     * Remove the selected manga from the library.
     *
     * @param mangas the list of manga to delete.
     * @param deleteChapters whether to also delete downloaded chapters.
     */
    fun removeMangaFromLibrary(mangas: List<Manga>, deleteChapters: Boolean) {
        // Create a set of the list
        val mangaToDelete = mangas.distinctBy {
            it.id
        }
        mangaToDelete.forEach {
            it.favorite = false
        }

        Observable
                .fromCallable {
                    databaseHelper.insertMangas(mangaToDelete).executeAsBlocking()
                }
                .onErrorResumeNext(Function {
                    Observable.empty()
                })
                .subscribeOn(Schedulers.io())
                .subscribe()

        Observable
                .fromCallable {
                    mangaToDelete.forEach { manga ->
                        coverCache.deleteFromCache(manga.thumbnail_url)
                        if (deleteChapters) {
                            val source = sourceManager.get(manga.source) as? HttpSource
                            if (source != null) {
                                downloadManger.deleteManga(manga, source)
                            }
                        }
                    }
                }
                .subscribeOn(Schedulers.io())
                .subscribe()

    }

    /**
     * Move the given list of manga to categories.
     *
     * @param categories the selected categories.
     * @param mangas the list of manga to move.
     */
    fun moveMangasToCategories(categories: List<Category>, mangas: List<Manga>) {
        val mangasCategories = ArrayList<MangaCategory>()

        for (manga in mangas) {
            for (cat in categories) {
                mangasCategories.add(MangaCategory.create(manga, cat))
            }
        }

        databaseHelper.setMangaCategories(mangasCategories, mangas)
    }

    /**
     * Update cover with local file.
     *
     * @param inputStream the new cover.
     * @param manga the manga edited.
     * @return true if the cover is updated, false otherwise
     */
    @Throws(IOException::class)
    fun editCoverWithStream(inputStream: InputStream, manga: Manga): Boolean {
        if (manga.source == LocalSource.ID) {
            LocalSource.updateCover(context, manga, inputStream)
            return true
        }

        if (manga.thumbnail_url != null && manga.favorite) {
            coverCache.copyToCache(manga.thumbnail_url!!, inputStream)
            return true
        }
        return false
    }
}