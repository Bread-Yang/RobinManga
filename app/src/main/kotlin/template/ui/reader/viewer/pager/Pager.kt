package template.ui.reader.viewer.pager

import android.support.v4.view.PagerAdapter
import android.view.ViewGroup
import io.reactivex.functions.Consumer

/**
 * Created by Robin Yeung on 8/25/18.
 */
public interface Pager {

    fun setId(id: Int)
    fun setLayoutParams(layoutParams: ViewGroup.LayoutParams)

    fun setOffscreenPageLimit(limit: Int)

    fun getCurrentItem(): Int
    fun setCurrentItem(item: Int, smoothScroll: Boolean)

    fun getWidth(): Int
    fun getHeight(): Int

    fun getAdapter(): PagerAdapter
    fun setAdapter(adapter: PagerAdapter)

    fun setOnChapterBoundariesOutListener(listener: OnChapterBoundariesOutListener)

    fun setOnPageChangeListener(onPageChanged: Consumer<Int>)
    fun clearOnPageChangeListeners()
}