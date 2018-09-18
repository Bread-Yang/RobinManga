package template.ui.reader.viewer.pager.horizontal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import template.ui.reader.viewer.pager.PagerReader

/**
 * Left to Right reader.
 */
class LeftToRightReader : PagerReader() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return HorizontalViewPager(activity!!).apply {
            initializePager(this)
        }
    }
}
