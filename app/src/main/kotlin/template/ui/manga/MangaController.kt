package template.ui.manga

import android.Manifest
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.View
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.support.RouterPagerAdapter
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.manga_controller.*
import nucleus5.factory.RequiresPresenter
import template.App
import template.R
import template.annotation.Layout
import template.data.database.models.Manga
import template.extensions.requestPermissionsSafe
import template.extensions.toast
import template.source.Source
import template.ui.base.controller.TabbedController
import template.ui.common.mvp.controller.NucleusDaggerController
import template.ui.manga.chapter.ChaptersController
import template.ui.manga.info.MangaInfoController
import java.util.*

/**
 * Created by Robin Yeung on 9/6/18.
 */
@Layout(R.layout.manga_controller)
@RequiresPresenter(MangaPresenter::class)
class MangaController: NucleusDaggerController<MangaPresenter>, TabbedController {

    constructor(manga: Manga?, fromCatalogue: Boolean = false) : super(Bundle().apply {
        putLong(MANGA_EXTRA, manga?.id ?: 0)
        putBoolean(FROM_CATALOGUE_EXTRA, fromCatalogue)
    }) {
        this.manga = manga
        if (manga != null) {
            source = App.app.lazySourceManager.get().getOrStub(manga.source)
        }
    }

    constructor(mangaId: Long) : this(
            App.app.lazyDatabaseHelper.get().getManga(mangaId).executeAsBlocking()
    )

    constructor(bundle: Bundle) : this(bundle.getLong(MANGA_EXTRA))

    var manga: Manga? = null
        private set

    var source: Source? = null
        private set

    private var adapter: MangaDetailAdapter? = null

    val fromCatalogue = args.getBoolean(FROM_CATALOGUE_EXTRA, false)

    val lastUpdateSubject: BehaviorSubject<Date> = BehaviorSubject.create()

    val chapterCountSubject: BehaviorSubject<Float> = BehaviorSubject.create()

    val mangaFavoriteSubject: PublishSubject<Boolean> = PublishSubject.create()

    private val trackingIconSubject: BehaviorSubject<Boolean> = BehaviorSubject.create()

    private var trackingIconDisposable: Disposable? = null

    override fun onViewCreated(view: View) {
        if (manga == null || source == null) return

        requestPermissionsSafe(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 301)

        adapter = MangaDetailAdapter()
        viewPager.offscreenPageLimit = 3
        viewPager.adapter = adapter

        if (!fromCatalogue)
            viewPager.currentItem = CHAPTERS_CONTROLLER
    }

    override fun initPresenterOnce() {
    }

    override fun onDestroyView(view: View) {
        super.onDestroyView(view)
        adapter = null
    }

    override fun onChangeStarted(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeStarted(changeHandler, changeType)
        if (changeType.isEnter) {
            activity?.tabLayout?.setupWithViewPager(viewPager)
            trackingIconDisposable = trackingIconSubject.subscribe {
                setTrackingIconInternal(it)
            }
        }
    }

    override fun onChangeEnded(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeEnded(changeHandler, changeType)
        if (manga == null || source == null) {
            activity?.toast(R.string.manga_not_in_db)
            router.popController(this)
        }
    }

    override fun configureTabs(tabs: TabLayout) {
        with(tabs) {
            tabGravity = TabLayout.GRAVITY_FILL
            tabMode = TabLayout.MODE_FIXED
        }
    }

    override fun cleanupTabs(tabs: TabLayout) {
        trackingIconDisposable?.dispose()
        setTrackingIconInternal(false)
    }

    fun setTrackingIcon(visible: Boolean) {
        trackingIconSubject.onNext(visible)
    }

    private fun setTrackingIconInternal(visible: Boolean) {
    }

    private inner class MangaDetailAdapter : RouterPagerAdapter(this@MangaController) {

        // TODO
        private val tabCount = 2

        private val tabTitles = listOf(
                R.string.manga_detail_tab,
                R.string.manga_chapters_tab,
                R.string.manga_tracking_tab)
                .map {
                    resources!!.getString(it)
                }

        override fun getCount(): Int {
            return tabCount
        }

        override fun configureRouter(router: Router, position: Int) {
            if (!router.hasRootController()) {
                val controller = when (position) {
                    INFO_CONTROLLER -> MangaInfoController()
                    CHAPTERS_CONTROLLER -> ChaptersController()
                    // TODO
                    else -> error("Wrong position $position")
                }
                router.setRoot(RouterTransaction.with(controller))
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return tabTitles[position]
        }
    }

    companion object {

        const val FROM_CATALOGUE_EXTRA = "from_catalogue"
        const val MANGA_EXTRA = "manga"

        const val INFO_CONTROLLER = 0
        const val CHAPTERS_CONTROLLER = 1
        const val TRACK_CONTROLLER = 2

        private val tabField = TabLayout.Tab::class.java.getDeclaredField("mView")
                .apply {
                    isAccessible = true
                }
    }
}