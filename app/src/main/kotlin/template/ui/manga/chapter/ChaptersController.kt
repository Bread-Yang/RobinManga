package template.ui.manga.chapter

import android.content.Intent
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import eu.davidea.flexibleadapter.FlexibleAdapter
import kotlinx.android.synthetic.main.chapters_controller.*
import nucleus5.factory.RequiresPresenter
import template.R
import template.annotation.Layout
import template.data.database.models.Chapter
import template.data.download.model.Download
import template.extensions.toast
import template.ui.common.mvp.controller.NucleusDaggerController
import template.ui.manga.MangaController
import template.ui.reader.ReaderActivity

@Layout(R.layout.chapters_controller)
@RequiresPresenter(ChaptersPresenter::class)
class ChaptersController : NucleusDaggerController<ChaptersPresenter>(),
        FlexibleAdapter.OnItemClickListener,
        ChaptersAdapter.OnMenuItemClickListener {

    /**
     * Adapter containing a list of chapters.
     */
    private var adapter: ChaptersAdapter? = null

    /**
     * Action mode for multiple selection.
     */
    private var actionMode: ActionMode? = null      // demo : http://www.androhub.com/android-contextual-action-mode-over-toolbar/

    /**
     * Selected items. Used to restore selections after a rotation.
     */
    private val selectedItems = mutableSetOf<ChapterItem>()

    override fun onViewCreated(view: View) {
        adapter = ChaptersAdapter(this, view.context)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(view.context)
        recyclerView.addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(view.context, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL))
        recyclerView.setHasFixedSize(true)
        adapter?.fastScroller = fastScroller

        swipeRefresh.refreshes().subscribeUntilDestroy {
            fetchChaptersFromSource()
        }
    }

    override fun initPresenterOnce() {
        val ctrl = parentController as MangaController
        presenter.init(ctrl)
    }

    override fun onDestroyView(view: View) {
        adapter = null
        actionMode = null
        super.onDestroyView(view)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chapters, menu)
    }

    fun onNextChapters(chapterItems: List<ChapterItem>) {
        // If the list is empty, fetch chapters from source if the conditions are met
        // We use presenter chapters instead because they are always unfiltered
        if (presenter.chapterItems.isEmpty()) {
            initialFetchChapters()
        }

        val adapter = adapter ?: return
        adapter.updateDataSet(chapterItems)

        if (selectedItems.isNotEmpty()) {
            adapter.clearSelection()    // we need to start from a clean state, index may have changed
            // TODO
        }
    }

    private fun initialFetchChapters() {
        // Only fecth if this view is from the catalog and it hasn't requested previously
        if ((parentController as MangaController).fromCatalogue && !presenter.hasRequested) {
            fetchChaptersFromSource()
        }
    }

    private fun fetchChaptersFromSource() {
        swipeRefresh?.isRefreshing = true
        presenter.fetchChaptersFromSource()
    }

    private fun downloadChapters(chapters: List<ChapterItem>) {
        val view = view
        // TODO
//        destroyActionModeIfNeeded()
        presenter.downloadChapters(chapters)
    }

    fun onFetchChaptersDone() {
        swipeRefresh?.isRefreshing = false
    }

    fun onFetchChaptersError(error: Throwable) {
        swipeRefresh?.isRefreshing = false
        activity?.toast(error.message)
    }

    override fun onMenuItemClick(position: Int, item: MenuItem) {
        val chapter = adapter?.getItem(position) ?: return
        val chapters = listOf(chapter)

        when (item.itemId) {
            R.id.action_download -> downloadChapters(chapters)
        }
    }

    override fun onItemClick(view: View?, position: Int): Boolean {
        val adapter = adapter ?: return false
        val item = adapter.getItem(position) ?: return false
        // TODO
        openChapter(item.chapter)
        return false
    }

    fun onChapterStatusChange(download: Download) {
        getHolder(download.chapter)?.notifyStatus(download.status)
    }

    private fun getHolder(chapter: Chapter): ChapterHolder? {
        return recyclerView?.findViewHolderForItemId(chapter.id!!) as? ChapterHolder
    }

    fun openChapter(chapter: Chapter, hasAnimation: Boolean = false) {
        val activity = activity ?: return
        val intent = ReaderActivity.newIntent(activity, presenter.manga, chapter)
        if (hasAnimation) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        startActivity(intent)
    }
}
