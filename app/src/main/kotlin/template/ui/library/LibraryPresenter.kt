package template.ui.library

import android.content.Context
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import template.data.cache.CoverCache
import template.data.database.DatabaseHelper
import template.data.database.models.Category
import template.data.database.models.Manga
import template.data.database.models.MangaCategory
import template.data.download.DownloadManager
import template.source.SourceManager
import template.ui.common.mvp.BasePresenter
import template.utils.preference.PreferencesHelper
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
}