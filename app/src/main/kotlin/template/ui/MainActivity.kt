package template.ui

import android.os.Bundle
import android.view.ViewGroup
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.android.synthetic.main.activity_main.*
import template.R
import template.ui.common.activity.BaseActivity
import template.ui.common.annotation.Layout
import template.ui.main.MainController

@Layout(R.layout.activity_main)
class MainActivity : BaseActivity() {

    var testData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        testData ?: let {
            testData = "haha"
        }
        if (!router.hasRootController()) router.pushController(RouterTransaction.with(MainController()))
    }

    override fun container(): ViewGroup = container

    companion object {
        // Shortcut actions
        const val SHORTCUT_LIBRARY = "eu.kanade.tachiyomi.SHOW_LIBRARY"
        const val SHORTCUT_RECENTLY_UPDATED = "eu.kanade.tachiyomi.SHOW_RECENTLY_UPDATED"
        const val SHORTCUT_RECENTLY_READ = "eu.kanade.tachiyomi.SHOW_RECENTLY_READ"
        const val SHORTCUT_CATALOGUES = "eu.kanade.tachiyomi.SHOW_CATALOGUES"
        const val SHORTCUT_DOWNLOADS = "eu.kanade.tachiyomi.SHOW_DOWNLOADS"
        const val SHORTCUT_MANGA = "eu.kanade.tachiyomi.SHOW_MANGA"
    }
}