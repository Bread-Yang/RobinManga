package template.ui.reader.viewer.pager

import androidx.viewpager.widget.PagerAdapter
import android.view.View
import android.view.ViewGroup
import template.R
import template.extensions.inflate
import template.source.model.Page
import template.widget.ViewPagerAdapter

/**
 * Adapter of pages for a ViewPager.
 */
class PagerReaderAdapter(private val reader: PagerReader) : ViewPagerAdapter() {

    /**
     * Pages stored in the adpater.
     */
    var pages: List<Page> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun createView(container: ViewGroup, position: Int): View {
        val view = container.inflate(R.layout.reader_pager_item) as PageView
        view.initialize(reader, pages[position])
        return view
    }

    /**
     * Returns the number of pages.
     */
    override fun getCount(): Int {
        return pages.size
    }

    override fun getItemPosition(obj: Any): Int {
        val view = obj as PageView
        return if (view.page in pages) {
            androidx.viewpager.widget.PagerAdapter.POSITION_UNCHANGED
        } else {
            androidx.viewpager.widget.PagerAdapter.POSITION_NONE
        }
    }
}
