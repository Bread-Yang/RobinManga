package template.ui.main

import android.content.Intent
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
import template.annotation.Layout
import template.annotation.Mockable
import template.extensions.withFadeTransaction
import template.ui.catalogue.browse.BrowseCatalogueController
import template.ui.common.mvp.activity.NucleusDaggerActivity
import template.ui.download.DownloadController
import template.ui.setting.SettingsMainController

@Mockable
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
        setTheme(when (presenter.preferencesHelper.theme()) {
            2 -> R.style.Theme_Tachiyomi_Dark
            3 -> R.style.Theme_Tachiyomi_Amoled
            else -> R.style.Theme_Tachiyomi
        })
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
                    R.id.nav_drawer_catalogues -> {
                        router.pushController(RouterTransaction.with(BrowseCatalogueController()))
                    }
                    R.id.nav_drawer_downloads -> {
                        router.pushController(DownloadController().withFadeTransaction())
                    }
                    R.id.nav_drawer_settings -> {
                        router.pushController(SettingsMainController().withFadeTransaction())
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

    override fun onNewIntent(intent: Intent) {
        if (!handleIntentAction(intent)) {
            super.onNewIntent(intent)
        }
    }

    private fun handleIntentAction(intent: Intent): Boolean {
        when (intent.action) {
            SHORTCUT_DOWNLOADS -> {
                if (router.backstack.none { it.controller() is DownloadController }) {
                    setSelectedDrawerItem(R.id.nav_drawer_downloads)
                }
            }
        }
        return true
    }

    private fun setSelectedDrawerItem(itemId: Int) {
        if (!isFinishing) {
            navigationView.setCheckedItem(itemId)
            navigationView.menu.performIdentifierAction(itemId, 0)
        }
    }

    override fun onBackPressed() {
        val backstackSize = router.backstackSize
        if (drawerLayout.isDrawerOpen(GravityCompat.START) || drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawers()
        } else if (backstackSize == 1 || !router.handleBack()) {
            super.onBackPressed()
        }
    }

    fun callByPresenter() {

    }
}