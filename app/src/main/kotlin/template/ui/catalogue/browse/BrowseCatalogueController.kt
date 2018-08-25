package template.ui.catalogue.browse

import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFlexible
import kotlinx.android.synthetic.main.controller_catalogue.*
import nucleus5.factory.RequiresPresenter
import template.R
import template.extensions.visible
import template.ui.common.annotation.Layout
import template.ui.common.mvp.controller.NucleusController

/**
 * Created by Robin Yeung on 8/22/18.
 */
@Layout(R.layout.controller_catalogue)
@RequiresPresenter(BrowseCataloguePresenter::class)
class BrowseCatalogueController : NucleusController<BrowseCataloguePresenter>() {

    /**
     * Adapter containing the list of manga from the catalogue.
     */
    private var adapter: FlexibleAdapter<IFlexible<*>>? = null

    /**
     * Recycler view with the list of results.
     */
    private var recycler: RecyclerView? = null

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        adapter = FlexibleAdapter(null)
        setupRecycler(view)

        progressBar?.visible()

        // 请求数据
        presenter.setSourceId(4)
        presenter.requestNext()
    }

    fun setupRecycler(view: View) {

        var oldPosition = RecyclerView.NO_POSITION
        val oldRecycler = lltCatalogue.getChildAt(1)
        if (oldRecycler is RecyclerView) {
            oldPosition = (oldRecycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            oldRecycler.adapter = null

            lltCatalogue.removeView(oldRecycler)
        }

        val recycler =
                RecyclerView(view.context).apply {
                    id = R.id.recyclerView
                    layoutManager = LinearLayoutManager(context)
                    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                }

        recycler.setHasFixedSize(true)
        recycler.adapter = adapter

        lltCatalogue.addView(recycler, 1)

        if (oldPosition != RecyclerView.NO_POSITION) {
            recycler.layoutManager.scrollToPosition(oldPosition)
        }
        this.recycler = recycler
    }

}