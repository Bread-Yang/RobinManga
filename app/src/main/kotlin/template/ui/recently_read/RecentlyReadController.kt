package template.ui.recently_read

import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import eu.davidea.flexibleadapter.FlexibleAdapter
import kotlinx.android.synthetic.main.recently_read_controller.*
import nucleus5.factory.RequiresPresenter
import template.R
import template.annotation.Layout
import template.data.database.models.History
import template.data.database.models.Manga
import template.extensions.toast
import template.extensions.withFadeTransaction
import template.ui.common.mvp.controller.NucleusDaggerController
import template.ui.manga.MangaController
import template.ui.reader.ReaderActivity

/**
 * Controller that shows recently read manga.
 * Use R.layout.recently_read_controller.
 * UI related actions should be called from here.
 */
@Layout(R.layout.recently_read_controller)
@RequiresPresenter(RecentlyReadPresenter::class)
class RecentlyReadController : NucleusDaggerController<RecentlyReadPresenter>(),
        FlexibleAdapter.OnUpdateListener,
        RecentlyReadAdapter.OnRemoveClickListener,
        RecentlyReadAdapter.OnResumeClickListener,
        RecentlyReadAdapter.OnCoverClickListener,
        RemoveHistoryDialog.Listener {

    /**
     * Adapter containing the recent manga.
     */
    var adapter: RecentlyReadAdapter? = null
        private set


    override fun onViewCreated(view: View) {
        // Initialize adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(view.context)
        adapter = RecentlyReadAdapter(this@RecentlyReadController)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }

    override fun initPresenterOnce() {
    }

    override fun onDestroyView(view: View) {
        adapter = null
        super.onDestroyView(view)
    }

    /**
     * Populate adapter with chapters
     *
     * @param mangaHistories list of manga history
     */
    fun onNextManga(mangaHistories: List<RecentlyReadItem>) {
        adapter?.updateDataSet(mangaHistories)
    }

    override fun onUpdateEmptyView(size: Int) {
        if (size > 0) {
            emptyView.hide()
        } else {
            emptyView.show(R.drawable.ic_glasses_black_24dp, R.string.information_no_recent_manga)
        }
    }

    override fun onResumeClick(position: Int) {
        val activity = activity ?: return
        val (manga, chapter, _) = adapter?.getItem(position)?.mch ?: return

        val nextChapter = presenter.getNextChapter(chapter, manga)
        if (nextChapter != null) {
            val intent = ReaderActivity.newIntent(activity, manga, nextChapter)
            startActivity(intent)
        } else {
            activity.toast(R.string.no_next_chapter)
        }
    }

    override fun onRemoveClick(position: Int) {
        val (manga, _, history) = adapter?.getItem(position)?.mch ?: return
        RemoveHistoryDialog(this, manga, history).showDialog(router)
    }

    override fun onCoverClick(position: Int) {
        val manga = adapter?.getItem(position)?.mch?.manga ?: return
        router.pushController(MangaController(manga).withFadeTransaction())
    }

    override fun removeHistory(manga: Manga, history: History, all: Boolean) {
        if (all) {
            // Reset last read of chapter to 0L
            presenter.removeAllFromHistory(manga.id!!)
        } else {
            // Remove all chapters belonging to manga from library
            presenter.removeFromHistory(history)
        }
    }
}
