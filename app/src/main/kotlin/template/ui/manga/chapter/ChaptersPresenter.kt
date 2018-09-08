package template.ui.manga.chapter

import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import template.data.database.models.Manga
import template.source.Source
import template.ui.common.mvp.BasePresenter
import template.ui.manga.MangaController
import java.util.*

/**
 * Created by Robin Yeung on 9/7/18.
 */
class ChaptersPresenter : BasePresenter<ChaptersController>() {

    lateinit var manga: Manga

    lateinit var source: Source

    lateinit var chapterCountSubject: BehaviorSubject<Float>

    lateinit var lastUpdateSubject: BehaviorSubject<Date>

    lateinit var mangaFavoriteSubject: PublishSubject<Boolean>

    fun init(parentcontroller: MangaController) {
        this.manga = parentcontroller.manga!!
        this.source = parentcontroller.source!!
        this.chapterCountSubject = parentcontroller.chapterCountSubject
        this.lastUpdateSubject = parentcontroller.lastUpdateSubject
        this.mangaFavoriteSubject = parentcontroller.mangaFavoriteSubject
    }
}