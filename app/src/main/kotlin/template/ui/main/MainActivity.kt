package template.ui.main

import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.graphics.drawable.DrawerArrowDrawable
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.android.synthetic.main.main_activity.*
import nucleus5.factory.RequiresPresenter
import template.R
import template.extensions.withFadeTransaction
import template.ui.catalogue.browse.BrowseCatalogueController
import template.ui.common.annotation.Layout
import template.ui.common.mvp.activity.NucleusDaggerActivity
import template.ui.download.DownloadController

@Layout(R.layout.main_activity)
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

    private var drawerArrow: DrawerArrowDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(toolbar)

        drawerArrow = DrawerArrowDrawable(this)
        drawerArrow?.color = Color.WHITE
        toolbar.navigationIcon = drawerArrow

        // Set behavior of Navigation drawer
        navigationView.setNavigationItemSelectedListener {
            val id = it.itemId

            val currentRoot = router.backstack.firstOrNull()
            if (currentRoot?.tag()?.toIntOrNull() != id) {
                when (id) {
                    // TODO
                    R.id.nav_drawer_downloads -> {
                        router.pushController(DownloadController().withFadeTransaction())
                    }
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        router = Conductor.attachRouter(this, controllerContainer, savedInstanceState)
        if (!router.hasRootController())
            router.pushController(RouterTransaction.with(BrowseCatalogueController()))

        toolbar.setNavigationOnClickListener {
            if (router.backstackSize == 1) {
                drawerLayout.openDrawer(GravityCompat.START)
            } else {
                onBackPressed()
            }
        }
    }

    override fun initPresenterOnce() {
    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }
}