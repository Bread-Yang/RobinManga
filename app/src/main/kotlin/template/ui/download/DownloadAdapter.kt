package template.ui.download

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import template.R
import template.data.download.model.Download
import template.extensions.inflate

/**
 * Adapter storing a list of download.
 *
 * @param context the context of the controller containing the adapter.
 */
class DownloadAdapter : RecyclerView.Adapter<DownloadHolder>() {

    private var items = emptyList<Download>()

    init {
        // RecycleView disabled stable IDs as default. So generally after notifyDataSetChanged(),
        // RecyclerView.Adapter didn’t assigned the same ViewHolder to original item in the data set.
        // After using stable Id, RecyclerView would try to use the same viewholder and view
        // for the same id. This would reduce blinking issue after data changed.
        setHasStableIds(true)
    }

    /**
     * Sets a list of downloads in the adapter.
     *
     * @param downloads the list to set.
     */
    fun setItems(downloads: List<Download>) {
        items = downloads
        notifyDataSetChanged()
    }

    /**
     * Returns the number of downloads in the adapter
     */
    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * Returns the identifier for a download.
     *
     * @param position the position in the adapter.
     * @return an identifier for the item.
     */
    override fun getItemId(position: Int): Long {
        return items[position].chapter.id!!
    }

    /**
     * Creates a new view holder.
     *
     * @param parent the parent view.
     * @param viewType the type of the holder.
     * @return a new view holder for a manga.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadHolder {
        val view = parent.inflate(R.layout.download_item)
        return DownloadHolder(view)
    }

    /**
     * Binds a holder with a new position.
     *
     * @param holder the holder to bind.
     * @param position the position to bind.
     */
    override fun onBindViewHolder(holder: DownloadHolder, position: Int) {
        val download = items[position]
        holder.onSetValues(download)
    }
}
