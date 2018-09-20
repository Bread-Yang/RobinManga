package template.ui.main

import android.os.Bundle
import template.ui.common.mvp.BasePresenter
import template.utils.preference.PreferencesHelper
import javax.inject.Inject

class MainPresenter : BasePresenter<MainActivity>() {

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
    }


}