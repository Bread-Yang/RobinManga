package template.ui.reader

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.reader_activity.*
import me.zhanghai.android.systemuihelper.SystemUiHelper
import nucleus5.factory.RequiresPresenter
import template.R
import template.data.database.models.Chapter
import template.data.database.models.Manga
import template.extensions.getOrDefault
import template.extensions.toast
import template.source.model.Page
import template.ui.common.annotation.Layout
import template.ui.common.mvp.activity.NucleusDaggerActivity
import template.ui.reader.viewer.base.BaseReader
import template.ui.reader.viewer.pager.horizontal.RightToLeftReader
import template.utils.GLUtil
import template.utils.SharedData
import timber.log.Timber
import java.text.DecimalFormat

@Layout(R.layout.reader_activity)
@RequiresPresenter(ReaderPresenter::class)
class ReaderActivity : NucleusDaggerActivity<ReaderPresenter>() {

    companion object {
        @Suppress("unused")
        const val LEFT_TO_RIGHT = 1
        const val RIGHT_TO_LEFT = 2
        const val VERTICAL = 3
        const val WEBTOON = 4

        const val WHITE_THEME = 0
        const val BLACK_THEME = 1

        const val MENU_VISIBLE = "menu_visible"

        fun newIntent(context: Context, manga: Manga, chapter: Chapter): Intent {
            SharedData.put(ReaderEvent(manga, chapter))
            return Intent(context, ReaderActivity::class.java)
        }
    }

    private var viewer: BaseReader? = null

    private var customBrightnessDisposable: Disposable? = null

    private var customFilterColorDisposable: Disposable? = null

    var readerTheme: Int = 0
        private set

    var maxBitmapSize: Int = 0
        private set

    private val decimalFortmat = DecimalFormat("#.###")

    val preferences by lazy {
        presenter.preferencesHelper
    }

    private val volumeKeysEnabled by lazy {
        preferences.readWithVolumeKeys().getOrDefault()
    }

    private val volumeKeysInverted by lazy {
        preferences.readWithVolumeKeysInverted().getOrDefault()
    }

    private var systemUi: SystemUiHelper? = null

    private var menuVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null && SharedData.get(ReaderEvent::class.java) == null) {
            finish()
            return
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        initializeSettings()
        initializeBottomMenu()

        if (savedInstanceState != null) {
            menuVisible = savedInstanceState.getBoolean(MENU_VISIBLE)
        }

        setMenuVisibility(menuVisible)

        maxBitmapSize = GLUtil.getMaxTextureSize()

        ibLeftChapter.setOnClickListener {
            if (viewer != null) {
                if (viewer is RightToLeftReader)
                    requestNextChapter()
                else
                    requestPreviousChapter()
            }
        }
        ibRightChapter.setOnClickListener {
            if (viewer != null) {
                if (viewer is RightToLeftReader)
                    requestPreviousChapter()
                else
                    requestNextChapter()
            }
        }
    }

    override fun initPresenterOnce() {

    }

    override fun onDestroy() {
        toolbar.setNavigationOnClickListener(null)
        viewer = null
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.reader, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // TODO
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(MENU_VISIBLE, menuVisible)
        super.onSaveInstanceState(outState)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setMenuVisibility(menuVisible, animate = false)
        }
    }

    override fun onBackPressed() {
        // TODO
        super.onBackPressed()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (!isFinishing) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    if (volumeKeysEnabled) {
                        if (event.action == KeyEvent.ACTION_UP) {
                            if (!volumeKeysInverted)
                                viewer?.moveDown()
                            else
                                viewer?.moveUp()
                        }
                        return true
                    }
                }
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    if (volumeKeysEnabled) {
                        if (event.action == KeyEvent.ACTION_UP) {
                            if (!volumeKeysInverted)
                                viewer?.moveUp()
                            else
                                viewer?.moveDown()
                        }
                        return true
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (!isFinishing) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_RIGHT -> viewer?.moveRight()
                KeyEvent.KEYCODE_DPAD_LEFT -> viewer?.moveLeft()
                KeyEvent.KEYCODE_DPAD_DOWN -> viewer?.moveDown()
                KeyEvent.KEYCODE_DPAD_UP -> viewer?.moveUp()
                KeyEvent.KEYCODE_PAGE_DOWN -> viewer?.moveDown()
                KeyEvent.KEYCODE_PAGE_UP -> viewer?.moveUp()
                KeyEvent.KEYCODE_MENU -> toggleMenu()
                else -> return super.onKeyUp(keyCode, event)
            }
        }
        return true
    }

    fun onChapterError(error: Throwable) {
        Timber.e(error)
        finish()
        toast(error.message)
    }

    fun onLongClick(page: Page) {
        // TODO
//        MaterialDialog.Builder(this)
//                .title(getString(R.string.options))
//                .items(R.array.reader_image_options)
//                .itemsIds(R.array.reader_image_options_values)
//                .itemsCallback { _, _, i, _ ->
//                    when (i) {
//                        0 -> setImageAsCover(page)
//                        1 -> shareImage(page)
//                        2 -> presenter.savePage(page)
//                    }
//                }.show()
    }

    fun onChapterAppendError() {
        // Ignore
    }

    /**
     * Called from the presenter at startup, allowing to prepare the selected reader.
     */
    fun onMangaOpen(manga: Manga) {
        if (viewer == null) {
            viewer = getOrCreateViewer(manga)
        }
        if (viewer is RightToLeftReader && seekbarPage.rotation != 180f) {
            // Invert the seeekbar for the right to left reader
            seekbarPage.rotation = 180f
        }
        supportActionBar?.title = manga.title
        pbPleaseWait.visibility = View.VISIBLE
        pbPleaseWait.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_long))
    }
}