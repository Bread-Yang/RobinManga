package template.ui.recently_read

import android.view.View
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.recently_read_item.*
import template.R
import template.data.database.models.MangaChapterHistory
import template.glide.GlideApp
import template.ui.base.holder.BaseFlexibleViewHolder
import java.util.*

/**
 * Holder that contains recent manga item
 * Use R.layout.recently_read_item.
 * UI related actions should be called from here.
 *
 * @param view the inflated view for this holder.
 * @param adapter the adapter handling this holder.
 * @constructor creates a new recent chapter holder.
 */
class RecentlyReadHolder(view: View, val adapter: RecentlyReadAdapter)
    : BaseFlexibleViewHolder(view, adapter) {

    init {
        btnRemove.setOnClickListener {
            adapter.removeClickListener.onRemoveClick(adapterPosition)
        }

        btnResume.setOnClickListener {
            adapter.resumeClickListener.onResumeClick(adapterPosition)
        }

        ivCover.setOnClickListener {
            adapter.coverClickListener.onCoverClick(adapterPosition)
        }
    }

    /**
     * Set values of view
     *
     * @param item item containing history information
     */
    fun bind(item: MangaChapterHistory) {
        // Retrieve objects
        val (manga, chapter, history) = item

        // Set manga title
        tvMangaTitle.text = manga.title

        // Set source + chapter title
        val formattedNumber = adapter.decimalFormat.format(chapter.chapter_number.toDouble())
        tvMangaSource.text = itemView.context.getString(R.string.recent_manga_source)
                .format(adapter.sourceManager.getOrStub(manga.source).toString(), formattedNumber)

        // Set last read timestamp title
        tvLastRead.text = adapter.dateFormat.format(Date(history.last_read))

        // Set cover
        GlideApp.with(itemView.context).clear(ivCover)
        if (!manga.thumbnail_url.isNullOrEmpty()) {
            GlideApp.with(itemView.context)
                    .load(manga)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .into(ivCover)
        }
    }
}
