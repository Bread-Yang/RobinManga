package template.ui.reader.viewer.pager

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.hippo.unifile.UniFile
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import kotlinx.android.synthetic.main.reader_pager_item.view.*
import template.R
import template.extensions.inflate
import template.source.model.Page
import template.ui.reader.ReaderActivity
import template.ui.reader.viewer.base.PageDecodeErrorLayout
import template.ui.reader.viewer.pager.horizontal.RightToLeftReader
import template.ui.reader.viewer.pager.vertical.VerticalReader
import java.lang.Exception
import java.util.concurrent.TimeUnit

class PageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null)
    : FrameLayout(context, attrs) {

    /**
     * Page of a chapter.
     */
    lateinit var page: Page

    /**
     * Disposable for status changes of the page.
     */
    private var statusDisposable: Disposable? = null

    /**
     * Disposable for progress changes of the page.
     */
    private var progressDisposable: Disposable? = null

    /**
     * Layout of decode error.
     */
    private var decodeErrorLayout: View? = null

    fun initialize(reader: PagerReader, page: Page) {
        val activity = reader.activity as ReaderActivity

        when (activity.readerTheme) {
            ReaderActivity.BLACK_THEME -> tvProgress.setTextColor(reader.whiteColor)
            ReaderActivity.WHITE_THEME -> tvProgress.setTextColor(reader.blackColor)
        }

        if (reader is RightToLeftReader) {
            rotation = 180f
        }

        with(imageView) {
            setMaxTileSize((reader.activity as ReaderActivity).maxBitmapSize)
            setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_FIXED)
            setDoubleTapZoomDuration(reader.doubleTagAnimDuration)
            setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_INSIDE)
            setMinimumScaleType(reader.scaleType)
            setMinimumDpi(90)
            setMinimumTileDpi(180)
            setRegionDecoderClass(reader.regionDecoderClass)
            setBitmapDecoderClass(reader.bitmapDecoderClass)
            setVerticalScrollingParent(reader is VerticalReader)
            setCropBorders(reader.cropBorders)
            setOnTouchListener { v, event -> reader.gestureDetector.onTouchEvent(event) }
            setOnLongClickListener { reader.onLongClick(page) }
            setOnImageEventListener(object : SubsamplingScaleImageView.DefaultOnImageEventListener() {
                override fun onReady() {
                    onImageDecoded(reader)
                }

                override fun onImageLoadError(p0: Exception?) {
                    onImageDecodeError(reader)
                }
            })
        }

        btnRetry.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                activity.presenter.retryPage(page)
            }
            true
        }

        this.page = page
        observeStatus()
    }

    override fun onDetachedFromWindow() {
        unsubscribeProgress()
        unsubscribeStatus()
        imageView.setOnTouchListener(null)
        imageView.setOnImageEventListener(null)
        super.onDetachedFromWindow()
    }

    /**
     * Observes the status of the page and notify the changes.
     *
     * @see processStatus
     */
    private fun observeStatus() {
        statusDisposable?.dispose()

        val statusProcessor = PublishProcessor.create<Int>().toSerialized()
        page.setStatusProcessor(statusProcessor)

        statusDisposable = statusProcessor.startWith(page.status)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    processStatus(it)
                }
    }

    /**
     * Observes the image download progress of the page and updates view.
     */
    private fun observeImageDownloadProgress() {
        progressDisposable?.dispose()

        progressDisposable = Flowable.interval(100, TimeUnit.MILLISECONDS)
                .map {
                    page.imageDownloadProgress
                }
                .distinctUntilChanged()
                .onBackpressureLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { progress ->
                    tvProgress.text = if (progress > 0) {
                        context.getString(R.string.download_progress, progress)
                    } else {
                        context.getString(R.string.downloading)
                    }
                }
    }

    /**
     * Called when the status of the page changes.
     *
     * @param status the new status of the page.
     */
    private fun processStatus(status: Int) {
        when (status) {
            Page.QUEUE -> setQueued()
            Page.LOAD_PAGE -> setLoading()
            Page.DOWNLOAD_IMAGE -> {
                observeImageDownloadProgress()
                setDownloading()
            }
            Page.READY -> {
                setImage()
                unsubscribeProgress()
            }
            Page.ERROR -> {
                setError()
                unsubscribeProgress()
            }
        }
    }

    /**
     * Unsubscribes from the status disposable.
     */
    private fun unsubscribeStatus() {
        page.setStatusProcessor(null)
        statusDisposable?.dispose()
        statusDisposable = null
    }

    /**
     * Unsubscribes from the progress disposable.
     */
    private fun unsubscribeProgress() {
        progressDisposable?.dispose()
        progressDisposable = null
    }

    /**
     * Called when the page is queued.
     */
    private fun setQueued() {
        lltProgressContainer.visibility = View.VISIBLE
        tvProgress.visibility = View.INVISIBLE
        btnRetry.visibility = View.GONE
        decodeErrorLayout?.let {
            removeView(it)
            decodeErrorLayout = null
        }
    }

    /**
     * Called when the page is loading.
     */
    private fun setLoading() {
        lltProgressContainer.visibility = View.VISIBLE
        tvProgress.visibility = View.VISIBLE
        tvProgress.setText(R.string.downloading)
    }

    /**
     * Called when the page is downloading.
     */
    private fun setDownloading() {
        lltProgressContainer.visibility = View.VISIBLE
        tvProgress.visibility = View.VISIBLE
    }

    /**
     * Called when the page is ready.
     */
    private fun setImage() {
        val uri = page.uri
        if (uri == null) {
            page.status = Page.ERROR
            return
        }

        val file = UniFile.fromUri(context, uri)
        if (!file.exists()) {
            page.status = Page.ERROR
            return
        }

        tvProgress.visibility = View.INVISIBLE
        imageView.setImage(ImageSource.uri(file.uri))
    }

    /**
     * Called when the page has an error.
     */
    private fun setError() {
        lltProgressContainer.visibility = View.GONE
        btnRetry.visibility = View.VISIBLE
    }

    /**
     * Called when the image is decoded and going to be displayed.
     */
    private fun onImageDecoded(reader: PagerReader) {
        lltProgressContainer.visibility = View.GONE

        with(imageView) {
            when (reader.zoomType) {
                PagerReader.ALIGN_LEFT -> setScaleAndCenter(scale, PointF(0f, 0f))
                PagerReader.ALIGN_RIGHT -> setScaleAndCenter(scale, PointF(sWidth.toFloat(), 0f))
                PagerReader.ALIGN_CENTER -> setScaleAndCenter(scale, center.apply { y = 0f })
            }
        }
    }

    /**
     * Called when an image fails to decode.
     */
    private fun onImageDecodeError(reader: PagerReader) {
        lltProgressContainer.visibility = View.GONE

        if (decodeErrorLayout != null || !reader.isAdded) return

        val activity = reader.activity as ReaderActivity

        val layout = inflate(R.layout.reader_page_decode_error)
        PageDecodeErrorLayout(layout, page, activity.readerTheme, {
            if (reader.isAdded) {
                activity.presenter.retryPage(page)
            }
        })
        decodeErrorLayout = layout
        addView(layout)
    }
}
