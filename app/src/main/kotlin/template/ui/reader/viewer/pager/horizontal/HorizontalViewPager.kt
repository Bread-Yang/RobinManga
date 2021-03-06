package template.ui.reader.viewer.pager.horizontal

import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.view.MotionEvent
import io.reactivex.functions.Consumer
import template.ui.reader.viewer.pager.OnChapterBoundariesOutListener
import template.ui.reader.viewer.pager.Pager

/**
 * Implementation of a [ViewPager] to add custom behavior on touch events.
 */
class HorizontalViewPager(context: Context) : androidx.viewpager.widget.ViewPager(context), Pager {

    companion object {

        const val SWIPE_TOLERANCE = 0.25f
    }

    private var onChapterBoundariesOutListener: OnChapterBoundariesOutListener? = null

    private var startDragX: Float = 0f

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        try {
            if (ev.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_DOWN) {
                if (currentItem == 0 || currentItem == adapter!!.count - 1) {
                    startDragX = ev.x
                }
            }

            return super.onInterceptTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            return false
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        try {
            onChapterBoundariesOutListener?.let { listener ->
                if (currentItem == 0) {
                    if (ev.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_UP) {
                        val displacement = ev.x - startDragX

                        if (ev.x > startDragX && displacement > width * SWIPE_TOLERANCE) {
                            listener.onFirstPageOutEvent()
                            return true
                        }

                        startDragX = 0f
                    }
                } else if (currentItem == adapter!!.count - 1) {
                    if (ev.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_UP) {
                        val displacement = startDragX - ev.x

                        if (ev.x < startDragX && displacement > width * SWIPE_TOLERANCE) {
                            listener.onLastPageOutEvent()
                            return true
                        }

                        startDragX = 0f
                    }
                }
            }

            return super.onTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            return false
        }

    }

    override fun setOnChapterBoundariesOutListener(listener: OnChapterBoundariesOutListener) {
        onChapterBoundariesOutListener = listener
    }

    override fun setOnPageChangeListener(onPageChanged: Consumer<Int>) {
        addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                onPageChanged.accept(position)
            }
        })
    }
}
