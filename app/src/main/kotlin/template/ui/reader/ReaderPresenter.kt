package template.ui.reader

import android.os.Bundle
import io.reactivex.Observable
import template.data.database.models.Chapter
import template.data.database.models.Manga
import template.ui.common.mvp.BasePresenter
import template.utils.SharedData
import template.utils.preference.PreferencesHelper
import javax.inject.Inject

/**
 * Created by Robin Yeung on 9/11/18.
 */
class ReaderPresenter : BasePresenter<ReaderActivity>() {

    companion object {
        /**
         * Id of the restartable that loads the active chapter.
         */
        private const val LOAD_ACTIVE_CHAPTER = 1
    }

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    /**
     * Manga being read.
     */
    lateinit var manga: Manga
        private set

    /**
     * Active chapter.
     */
    lateinit var chapter: ReaderChapter
        private set

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)

        if (savedState == null) {
            val event = SharedData.get(ReaderEvent::class.java) ?: return
            manga = event.manga
            chapter = event.chapter.toModel()
        } else {
            manga = savedState.getSerializable(ReaderPresenter::manga.name) as Manga
            chapter = savedState.getSerializable(ReaderPresenter::chapter.name) as ReaderChapter
        }
        // Send the active manga to the view to initialize the reader.
        Observable.just(manga)
                .subscribeLatestCache { view, manga ->
                    view.onMangaOpen(manga)
                }

        // Retrieve the sync list if auto syncing is enabled.
        // TODO

        restartableLatestCache(LOAD_ACTIVE_CHAPTER,
                {
                    loadChapterObservable(chapter)
                },
                { view, _ ->
                    view.onChapterReady(this.chapter)
                },
                { view, error ->
                    view.onChapterError(error)
                })

        if (savedState == null) {
            loadChapter(chapter)
        }
    }

    /**
     * Converts a chapter to a [ReaderChapter] if needed.
     */
    private fun Chapter.toModel(): ReaderChapter {
        if (this is ReaderChapter)
            return this
        return ReaderChapter(this)
    }

    /**
     * Returns an observable that loads the given chapter, discarding any previous work.
     *
     * @param chapter the now active chapter.
     */
    private fun loadChapterObservable(chapter: ReaderChapter): Observable<ReaderChapter> {
        loader
    }
}