package template.ui.reader

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.reader_activity.*
import me.zhanghai.android.systemuihelper.SystemUiHelper
import nucleus5.factory.RequiresPresenter
import template.R
import template.annotation.Layout
import template.data.database.models.Chapter
import template.data.database.models.Manga
import template.extensions.getOrDefault
import template.extensions.getUriCompat
import template.extensions.gone
import template.extensions.toast
import template.source.model.Page
import template.ui.common.mvp.activity.NucleusDaggerActivity
import template.ui.reader.viewer.base.BaseReader
import template.ui.reader.viewer.pager.horizontal.LeftToRightReader
import template.ui.reader.viewer.pager.horizontal.RightToLeftReader
import template.ui.reader.viewer.pager.vertical.VerticalReader
import template.utils.GLUtil
import template.utils.SharedData
import template.widget.SimpleAnimationListener
import timber.log.Timber
import java.io.File
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

    private val decimalFormat = DecimalFormat("#.###")

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

//        initializeSettings()
//        initializeBottomMenu()

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
//            setMenuVisibility(menuVisible, animate = false)
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
//                KeyEvent.KEYCODE_MENU -> toggleMenu()
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
        MaterialDialog.Builder(this)
                .title(getString(R.string.options))
                .items(R.array.reader_image_options)
                .itemsIds(R.array.reader_image_options_values)
                .itemsCallback { _, _, i, _ ->
                    when (i) {
                        0 -> setImageAsCover(page)
                        1 -> shareImage(page)
                        2 -> presenter.savePage(page)
                    }
                }.show()
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

    fun onChapterReady(chapter: ReaderChapter) {
        pbPleaseWait.gone()
        val pages = chapter.pages ?: run {
            onChapterError(Exception("Null Pages"))
            return
        }

        val activePage = pages.getOrElse(chapter.requestedPage) {
            pages.first()
        }

        viewer?.onPageListReady(chapter, activePage)
        setActiveChapter(chapter, activePage.index)
    }

    fun onEnterChapter(chapter: ReaderChapter, currentPage: Int) {
        val activePage = if (currentPage == -1) chapter.pages!!.lastIndex else currentPage
        presenter.setActiveChapter(chapter)
        setActiveChapter(chapter, activePage)
    }

    fun setActiveChapter(chapter: ReaderChapter, currentPage: Int) {
        val numPages = chapter.pages!!.size
        if (seekbarPage.rotation != 180f) {
            tvRightPage.text = "$numPages"
            tvLeftPage.text = "${currentPage + 1}"
        } else {
            tvLeftPage.text = "$numPages"
            tvRightPage.text = "${currentPage + 1}"
        }
        seekbarPage.max = numPages - 1
        seekbarPage.progress = currentPage

        supportActionBar?.subtitle = if (chapter.isRecognizedNumber)
            getString(R.string.chapter_subtitle, decimalFormat.format(chapter.chapter_number.toDouble()))
        else
            chapter.name
    }

    fun onAppendChapter(chapter: ReaderChapter) {
        viewer?.onPageListAppendReady(chapter)
    }

    fun onAdjacentChapters(previous: Chapter?, next: Chapter?) {
        val isInverted = viewer is RightToLeftReader

        // Chapters are inverted for the right to left reader
        val hasRightChapter = (if (isInverted) previous else next) != null
        val hasLeftChapter = (if (isInverted) next else previous) != null

        ibRightChapter.isEnabled = hasRightChapter
        ibRightChapter.alpha = if (hasRightChapter) 1f else 0.4f

        ibLeftChapter.isEnabled = hasLeftChapter
        ibLeftChapter.alpha = if (hasLeftChapter) 1f else 0.4f
    }

    private fun getOrCreateViewer(manga: Manga): BaseReader {
        val mangaViewer = if (manga.viewer == 0) preferences.defaultViewer() else manga.viewer

        // Try to reuse the viewer using its tag
        var fragment = supportFragmentManager.findFragmentByTag(manga.viewer.toString()) as? BaseReader
        if (fragment == null) {
            // Create a new viewer
            fragment = when (mangaViewer) {
                RIGHT_TO_LEFT -> RightToLeftReader()
                VERTICAL -> VerticalReader()
//                WEBTOON -> WebtoonReader()
                else -> LeftToRightReader()
            }

            supportFragmentManager.beginTransaction().replace(R.id.fltReader, fragment, manga.viewer.toString()).commit()
        }
        return fragment
    }

    fun onPageChanged(page: Page) {
        presenter.onPageChanged(page)

        val pageNumber = page.number
        val pageCount = page.chapter.pages!!.size
        tvPageNumber.text = "$pageNumber/$pageCount"
        if (seekbarPage.rotation != 180f) {
            tvLeftPage.text = "$pageNumber"
        } else {
            tvRightPage.text = "$pageNumber"
        }
        seekbarPage.progress = page.index
    }

    fun toggleMenu() {
        setMenuVisibility(!menuVisible)
    }

    fun requestNextChapter() {
        if (!presenter.loadNextChapter()) {
            toast(R.string.no_next_chapter)
        }
    }

    fun requestPreviousChapter() {
        if (!presenter.loadPreviousChapter()) {
            toast(R.string.no_previous_chapter)
        }
    }

    private fun setMenuVisibility(visible: Boolean, animate: Boolean = true) {
        menuVisible = visible
        if (visible) {
            systemUi?.show()
            fltReaderMenu.visibility = View.VISIBLE

            if (animate) {
                val toolbarAnimation = AnimationUtils.loadAnimation(this, R.anim.enter_from_top)
                toolbarAnimation.setAnimationListener(object : SimpleAnimationListener() {
                    override fun onAnimationStart(animation: Animation) {
                        // Fix status bar being translucent the first time it's opened.
                        if (Build.VERSION.SDK_INT >= 21) {
                            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        }
                    }
                })
                toolbar.startAnimation(toolbarAnimation)

                val bottomMenuAnimation = AnimationUtils.loadAnimation(this, R.anim.enter_from_bottom)
                lltReaderMenuBottom.startAnimation(bottomMenuAnimation)
            }
        } else {
            systemUi?.hide()

            if (animate) {
                val toolbarAnimation = AnimationUtils.loadAnimation(this, R.anim.exit_to_top)
                toolbarAnimation.setAnimationListener(object : SimpleAnimationListener() {
                    override fun onAnimationEnd(animation: Animation) {
                        fltReaderMenu.visibility = View.GONE
                    }
                })
                toolbar.startAnimation(toolbarAnimation)

                val bottomMenuAnimation = AnimationUtils.loadAnimation(this, R.anim.exit_to_bottom)
                lltReaderMenuBottom.startAnimation(bottomMenuAnimation)
            }
        }
    }

    /**
     * Start a share intent that lets user share image
     *
     * @param page page object containing image information.
     */
    private fun shareImage(page: Page) {
        if (page.status != Page.READY)
            return

        var uri = page.uri ?: return
        if (uri.toString().startsWith("file://")) {
            uri = File(uri.toString().substringAfter("file://")).getUriCompat(this)
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uri)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            type = "image/*"
        }
        startActivity(Intent.createChooser(intent, getString(R.string.action_share)))
    }

    /**
     * Sets the given page as the cover of the manga.
     *
     * @param page the page containing the image to set as cover.
     */
    private fun setImageAsCover(page: Page) {
        if (page.status != Page.READY)
            return

        MaterialDialog.Builder(this)
                .content(getString(R.string.confirm_set_image_as_cover))
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive { _, _ -> presenter.setImageAsCover(page) }
                .show()
    }
}