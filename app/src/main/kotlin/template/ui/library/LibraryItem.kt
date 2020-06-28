package template.ui.library

import androidx.recyclerview.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.f2prateek.rx.preferences2.Preference
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFilterable
import eu.davidea.flexibleadapter.items.IFlexible
import kotlinx.android.synthetic.main.catalogue_grid_item.view.*
import template.R
import template.data.database.models.LibraryManga
import template.extensions.getOrDefault
import template.widget.AutofitRecyclerView

class LibraryItem(val manga: LibraryManga, private val libraryAsList: Preference<Boolean>) :
        AbstractFlexibleItem<LibraryHolder>(), IFilterable<String> {

    var downloadCount = -1

    override fun getLayoutRes(): Int {
        return if (libraryAsList.getOrDefault())
            R.layout.catalogue_list_item
        else
            R.layout.catalogue_grid_item
    }

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<IFlexible<androidx.recyclerview.widget.RecyclerView.ViewHolder>>): LibraryHolder {
        val parent = adapter.recyclerView
        return if (parent is AutofitRecyclerView) {
            view.apply {
                val coverHeight = parent.itemWidth / 3 * 4
                fltCard.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, coverHeight)
                viewGradient.layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, coverHeight / 2, Gravity.BOTTOM)
            }
            LibraryGridHolder(view, adapter)
        } else {
            LibraryListHolder(view, adapter)
        }
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<androidx.recyclerview.widget.RecyclerView.ViewHolder>>?,
                                holder: LibraryHolder,
                                position: Int,
                                payloads: MutableList<Any>?) {
        holder.onSetValues(this)
    }

    /**
     * Filters a manga depending on a query.
     *
     * @param constraint the query to apply.
     * @return true if the manga should be included, false otherwise.
     */
    override fun filter(constraint: String): Boolean {
        return manga.title.contains(constraint, true) ||
                (manga.author?.contains(constraint, true) ?: false)
    }

    override fun equals(other: Any?): Boolean {
        if (other is LibraryItem) {
            return manga.id == other.manga.id
        }
        return false
    }

    override fun hashCode(): Int {
        return manga.id!!.hashCode()
    }
}
