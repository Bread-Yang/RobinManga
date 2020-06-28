package template.ui.main

import android.os.Bundle
import template.data.preference.PreferencesHelper
import template.ui.common.mvp.BasePresenter
import javax.inject.Inject

class MainPresenter : BasePresenter<MainActivity>() {

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
    }

    fun testUnitTest() {
        view?.callByPresenter()
    }
}