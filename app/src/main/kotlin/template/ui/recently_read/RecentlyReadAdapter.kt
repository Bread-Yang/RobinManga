package template.ui.recently_read

import eu.davidea.flexibleadapter.FlexibleAdapter
import template.App
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

/**
 * Adapter of RecentlyReadHolder.
 * Connection between Controller and Holder
 * Holder updates should be called from here.
 *
 * @param controller a RecentlyReadController object
 * @constructor creates an instance of the adapter.
 */
class RecentlyReadAdapter(controller: RecentlyReadController)
    : FlexibleAdapter<RecentlyReadItem>(null, controller, true) {

    val sourceManager = App.app.lazySourceManager.get()

    val resumeClickListener: OnResumeClickListener = controller

    val removeClickListener: OnRemoveClickListener = controller

    val coverClickListener: OnCoverClickListener = controller

    /**
     * DecimalFormat used to display correct chapter number
     */
    val decimalFormat = DecimalFormat("#.###", DecimalFormatSymbols()
            .apply { decimalSeparator = '.' })

    val dateFormat: DateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

    interface OnResumeClickListener {
        fun onResumeClick(position: Int)
    }

    interface OnRemoveClickListener {
        fun onRemoveClick(position: Int)
    }

    interface OnCoverClickListener {
        fun onCoverClick(position: Int)
    }
}
