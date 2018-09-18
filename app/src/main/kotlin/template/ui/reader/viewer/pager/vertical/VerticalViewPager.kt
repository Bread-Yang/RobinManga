package template.ui.reader.viewer.pager.vertical

import android.content.Context
import android.view.MotionEvent
import io.reactivex.functions.Consumer
import template.ui.reader.viewer.pager.OnChapterBoundariesOutListener
import template.ui.reader.viewer.pager.Pager

/**
 * Implementation of a [VerticalViewPagerImpl] to add custom behavior on touch events.
 */
class VerticalViewPager(context: Context) : VerticalViewPagerImpl(context), Pager {

    companion object {

        private val SWIPE_TOLERANCE = 0.25f
    }

    private var onChapterBoundariesOutListener: OnChapterBoundariesOutListener? = null
    private var startDragY: Float = 0.toFloat()

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        try {
            if (ev.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_DOWN) {
                if (currentItem == 0 || currentItem == adapter.count - 1) {
                    startDragY = ev.y
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
                        val displacement = ev.y - startDragY

                        if (ev.y > startDragY && displacement > height * SWIPE_TOLERANCE) {
                            listener.onFirstPageOutEvent()
                            return true
                        }

                        startDragY = 0f
                    }
                } else if (currentItem == adapter.count - 1) {
                    if (ev.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_UP) {
                        val displacement = startDragY - ev.y

                        if (ev.y < startDragY && displacement > height * SWIPE_TOLERANCE) {
                            listener.onLastPageOutEvent()
                            return true
                        }

                        startDragY = 0f
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

    override fun setOnPageChangeListener(func: Consumer<Int>) {
        addOnPageChangeListener(object : VerticalViewPagerImpl.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                func.accept(position)
            }
        })
    }
}
