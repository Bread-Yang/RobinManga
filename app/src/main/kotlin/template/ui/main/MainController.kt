package template.ui.main

import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import kotlinx.android.synthetic.main.controller_main.*
import kotlinx.android.synthetic.main.controller_main.view.*
import nucleus5.factory.RequiresPresenter
import template.R
import template.ui.catalogue.browse.BrowseCatalogueController
import template.ui.common.annotation.Layout
import template.ui.common.mvp.controller.NucleusController
import template.ui.detail.DetailController
import timber.log.Timber

@Layout(R.layout.controller_main)
@RequiresPresenter(MainPresenter::class)
class MainController : NucleusController<MainPresenter>() {

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        Timber.e("onViewCreated()")
        with(view.recyclerView) {
            layoutManager = LinearLayoutManager(view.context)
            addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.HORIZONTAL))
//            adapter = mainAdapter
        }
        if (tvLicense.text == "2") {
            tvLicense.text = "1"
        } else {
            tvLicense.text = "2"
        }
        tvLicense.setOnClickListener{
            toBrowseCatalogueController()
        }
    }

    override fun initPresenter() {
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        Timber.e("onAttach()")
    }

    override fun onDetach(view: View) {
        super.onDetach(view)
        Timber.e("onDetach()")
    }

    override fun onDestroyView(view: View) {
        super.onDestroyView(view)
        Timber.e("onDestroyView()")
    }

    fun onError(throwable: Throwable) {
        Timber.d(throwable)
        Toast.makeText(activity, throwable.message, Toast.LENGTH_LONG).show()
    }

    private fun goToDetails() {
        val toController = DetailController()
        router.pushController(RouterTransaction.with(toController)
                .pushChangeHandler(HorizontalChangeHandler())
                .popChangeHandler(HorizontalChangeHandler()))
    }

    private fun toBrowseCatalogueController() {
        val toController = BrowseCatalogueController()
        router.pushController(RouterTransaction.with(toController)
                .pushChangeHandler(HorizontalChangeHandler())
                .popChangeHandler(HorizontalChangeHandler()))
    }
}