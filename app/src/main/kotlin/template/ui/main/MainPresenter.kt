package template.ui.main

import android.os.Bundle
import template.api.Api
import template.ui.common.mvp.BasePresenter
import javax.inject.Inject

class MainPresenter : BasePresenter<MainActivity>() {

    @Inject
    lateinit var api: Api

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
    }
}