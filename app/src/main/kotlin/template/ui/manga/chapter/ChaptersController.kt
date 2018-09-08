package template.ui.manga.chapter

import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.chapters_controller.*
import nucleus5.factory.RequiresPresenter
import template.R
import template.ui.common.annotation.Layout
import template.ui.common.mvp.controller.NucleusController
import template.ui.manga.MangaController

/**
 * Created by Robin Yeung on 9/7/18.
 */
@Layout(R.layout.chapters_controller)
@RequiresPresenter(ChaptersPresenter::class)
class ChaptersController : NucleusController<ChaptersPresenter>(),
        ChaptersAdapter.OnMenuItemClickListener {

    /**
     * Adapter containing a list of chapters.
     */
    private var adapter: ChaptersAdapter? = null

    override fun onViewCreated(view: View) {
        adapter = ChaptersAdapter(this, view.context)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(view.context)
    }

    override fun initPresenterOnce() {
        val ctrl = parentController as MangaController
        presenter.init(ctrl)
    }

    override fun onMenuItemClick(position: Int, item: MenuItem) {
    }
}
