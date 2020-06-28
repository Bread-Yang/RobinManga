package template.ui.recently_read

import android.support.v7.widget.RecyclerView
import android.view.View
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import template.R
import template.data.database.models.MangaChapterHistory

class RecentlyReadItem(val mch: MangaChapterHistory) : AbstractFlexibleItem<RecentlyReadHolder>() {

    override fun getLayoutRes(): Int {
        return R.layout.recently_read_item
    }

    override fun createViewHolder(view: View,
                                  adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>)
            : RecentlyReadHolder {
        return RecentlyReadHolder(view, adapter as RecentlyReadAdapter)
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?,
                                holder: RecentlyReadHolder,
                                position: Int,
                                payloads: MutableList<Any>?) {
        holder.bind(mch)
    }

    override fun equals(other: Any?): Boolean {
        if (other is RecentlyReadItem) {
            return mch.manga.id == other.mch.manga.id
        }
        return false
    }

    override fun hashCode(): Int {
        return mch.manga.id!!.hashCode()
    }
}
