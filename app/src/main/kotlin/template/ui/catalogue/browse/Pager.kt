package template.ui.catalogue.browse

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import template.source.model.MangasPage
import template.source.model.SManga

/**
 * Pager,可以理解为传呼机，由用户A打给总台，总台再通知给用户B。这里的requestNext()相当于用户A，
 * 成员变量results相当于总台，监听与results的observer相当于用户B
 */
abstract class Pager(var currentPage: Int = 1) {

    var hasNextPage = true
        private set

    protected val resultsPublicSubject: PublishSubject<Pair<Int, List<SManga>>> = PublishSubject.create()

    fun resultsPublicSubject(): Observable<Pair<Int, List<SManga>>> {
        return resultsPublicSubject
    }

    abstract fun requestNext(): Observable<MangasPage>

    fun onPageReceived(mangasPage: MangasPage) {
        val page = currentPage
        currentPage++
        hasNextPage = mangasPage.hasNextPage && !mangasPage.mangas.isEmpty()
        resultsPublicSubject.onNext(Pair(page, mangasPage.mangas))
    }
}
