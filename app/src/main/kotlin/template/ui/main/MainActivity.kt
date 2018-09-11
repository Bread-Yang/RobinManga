package template.ui.main

import android.os.Bundle
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.android.synthetic.main.activity_main.*
import nucleus5.factory.RequiresPresenter
import template.R
import template.ui.catalogue.browse.BrowseCatalogueController
import template.ui.common.annotation.Layout
import template.ui.common.mvp.activity.NucleusDaggerActivity

@Layout(R.layout.activity_main)
@RequiresPresenter(MainPresenter::class)
class MainActivity : NucleusDaggerActivity<MainPresenter>() {

    companion object {
        // Shortcut actions
        const val SHORTCUT_LIBRARY = "eu.kanade.tachiyomi.SHOW_LIBRARY"
        const val SHORTCUT_RECENTLY_UPDATED = "eu.kanade.tachiyomi.SHOW_RECENTLY_UPDATED"
        const val SHORTCUT_RECENTLY_READ = "eu.kanade.tachiyomi.SHOW_RECENTLY_READ"
        const val SHORTCUT_CATALOGUES = "eu.kanade.tachiyomi.SHOW_CATALOGUES"
        const val SHORTCUT_DOWNLOADS = "eu.kanade.tachiyomi.SHOW_DOWNLOADS"
        const val SHORTCUT_MANGA = "eu.kanade.tachiyomi.SHOW_MANGA"
    }

    private lateinit var router: Router

    var testData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        testData ?: let {
            testData = "haha"
        }

        router = Conductor.attachRouter(this, container, savedInstanceState)
        if (!router.hasRootController()) router.pushController(RouterTransaction.with(BrowseCatalogueController()))
    }

    override fun initPresenterOnce() {
    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }
}