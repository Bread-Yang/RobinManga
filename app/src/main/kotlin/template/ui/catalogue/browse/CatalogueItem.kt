package template.ui.catalogue.browse

import android.support.v7.widget.RecyclerView
import android.view.View
import com.f2prateek.rx.preferences2.Preference
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import template.R
import template.data.database.models.Manga
import template.extensions.getOrDefault

class CatalogueItem(val manga: Manga, private val catalogueAsList: Preference<Boolean>) :
        AbstractFlexibleItem<CatalogueHolder>() {

    override fun getLayoutRes(): Int {
        return if (catalogueAsList.getOrDefault())
            R.layout.catalogue_grid_item
        else
            R.layout.catalogue_list_item
    }

    override fun createViewHolder(view: View,
                                  adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>): CatalogueHolder {
        val parent = adapter.recyclerView
        // TODO("实现Grid布局 ")
        return CatalogueListHolder(view, adapter)
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?,
                                holder: CatalogueHolder,
                                position: Int,
                                payloads: MutableList<Any>?) {
        holder.onSetValues(manga)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is CatalogueItem) {
            return manga.id!! == other.manga.id!!
        }
        return false
    }

    override fun hashCode(): Int {
        return manga.id!!.hashCode()
    }
}