package template.ui.reader.viewer.pager.vertical

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import template.ui.reader.viewer.pager.PagerReader

/**
 * Vertical reader.
 */
class VerticalReader : PagerReader() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View? {
        return VerticalViewPager(activity!!).apply { initializePager(this) }
    }

}
