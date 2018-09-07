package template.ui.detail

import android.view.View
import nucleus5.factory.RequiresPresenter
import template.R
import template.ui.common.annotation.Layout
import template.ui.common.mvp.controller.NucleusController

@Layout(R.layout.controller_detail)
@RequiresPresenter(DetailPresenter::class)
class DetailController : NucleusController<DetailPresenter>() {

    override fun onViewCreated(view: View) {
    }

    override fun initPresenter() {
    }
}