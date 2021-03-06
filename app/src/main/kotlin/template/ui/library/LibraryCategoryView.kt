package template.ui.library

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.SelectableAdapter
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.library_category.view.*
import template.App
import template.R
import template.data.database.models.Category
import template.data.database.models.Manga
import template.data.library.LibraryUpdateService
import template.extensions.getOrDefault
import template.extensions.inflate
import template.extensions.plusAssign
import template.extensions.toast
import template.data.preference.PreferencesHelper
import template.widget.AutofitRecyclerView

/**
 * Fragment containing the library manga for a certain category.
 */
class LibraryCategoryView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
        FrameLayout(context, attrs),
        FlexibleAdapter.OnItemClickListener,
        FlexibleAdapter.OnItemLongClickListener {

    /**
     * Preferences.
     */
    private val preferences: PreferencesHelper = App.app.lazyPreferencesHelper.get()

    /**
     * The fragment containing this view.
     */
    private lateinit var controller: LibraryController

    /**
     * Category for this view.
     */
    lateinit var category: Category
        private set

    /**
     * Recycler view of the list of manga.
     */
    private lateinit var recycler: androidx.recyclerview.widget.RecyclerView

    /**
     * Adapter to hold the manga in this category.
     */
    private lateinit var adapter: LibraryCategoryAdapter

    /**
     * Disposables while the view is bound.
     */
    private var disposables = CompositeDisposable()

    fun onCreate(controller: LibraryController) {
        this.controller = controller

        recycler = if (preferences.libraryAsList().getOrDefault()) {
            (swipeRefresh.inflate(R.layout.library_list_recycler) as androidx.recyclerview.widget.RecyclerView).apply {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            }
        } else {
            (swipeRefresh.inflate(R.layout.library_grid_recycler) as AutofitRecyclerView).apply {
                spanCount = controller.mangaPerRow
            }
        }

        adapter = LibraryCategoryAdapter(this)

        recycler.setHasFixedSize(true)
        recycler.adapter = adapter
        swipeRefresh.addView(recycler)

        recycler.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: androidx.recyclerview.widget.RecyclerView, newState: Int) {
                // Disable swipe refresh when view is not at the top
                val firstPos = (recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager)
                        .findFirstCompletelyVisibleItemPosition()
                swipeRefresh.isEnabled = firstPos <= 0
            }
        })

        // Double the distance required to trigger sync
        swipeRefresh.setDistanceToTriggerSync((2 * 64 * resources.displayMetrics.density).toInt())
        swipeRefresh.setOnRefreshListener {
            if (!LibraryUpdateService.isRunning(context)) {
                LibraryUpdateService.start(context, category)
                context.toast(R.string.updating_category)
            }
            // It can be a very long operation, so we disable swipe refresh and show a toast.
            swipeRefresh.isRefreshing = false
        }
    }

    fun onBind(category: Category) {
        this.category = category

        adapter.mode = if (controller.selectedMangas.isNotEmpty()) {
            SelectableAdapter.Mode.MULTI
        } else {
            SelectableAdapter.Mode.SINGLE
        }

        disposables += controller.searchChangeSubject
                .doOnNext {
                    adapter.setFilter(it)
                }
                .skip(1)
                .subscribe {
                    adapter.performFilter()
                }

        disposables += controller.libraryMangaUpdateSubject
                .subscribe {
                    onNextLibraryManga(it)
                }

        disposables += controller.selectionUpdateSubject
                .subscribe {
                    onSelectionChanged(it)
                }
    }

    fun onRecycle() {
        adapter.setItems(emptyList())
        adapter.clearSelection()
        disposable()
    }

    fun disposable() {
        disposables.clear()
    }

    /**
     * Subscribe to [LibraryMangaEvent]. When an event is received, it updates the content of the
     * adapter.
     *
     * @param event the event received.
     */
    fun onNextLibraryManga(event: LibraryMangaEvent) {
        // Get the manga list for this category.
        val mangaForCategory = event.getMangaForCategory(category).orEmpty()

        // Update the category with its manga.
        adapter.setItems(mangaForCategory)

        if (adapter.mode == SelectableAdapter.Mode.MULTI) {
            controller.selectedMangas.forEach {
                val position = adapter.indexOf(it)
                if (position != -1 && !adapter.isSelected(position)) {
                    adapter.toggleSelection(position)
                    (recycler.findViewHolderForItemId(it.id!!) as? LibraryHolder)?.toggleActivation()
                }
            }
        }
    }

    /**
     * Subscribe to [LibrarySelectionEvent]. When an event is received, it updates the selection
     * depending on the type of event received.
     *
     * @param event the selection event received.
     */
    private fun onSelectionChanged(event: LibrarySelectionEvent) {
        when (event) {
            is LibrarySelectionEvent.Selected -> {
                if (adapter.mode != SelectableAdapter.Mode.MULTI) {
                    adapter.mode = SelectableAdapter.Mode.MULTI
                }
                findAndToggleSelection(event.manga)
            }
            is LibrarySelectionEvent.Unselected -> {
                findAndToggleSelection(event.manga)
                if (controller.selectedMangas.isEmpty()) {
                    adapter.mode = SelectableAdapter.Mode.SINGLE
                }
            }
            is LibrarySelectionEvent.Cleared -> {
                adapter.mode = SelectableAdapter.Mode.SINGLE
                adapter.clearSelection()
            }
        }
    }

    /**
     * Toggles the selection for the given manga and updates the view if needed.
     *
     * @param manga the manga to toggle.
     */
    private fun findAndToggleSelection(manga: Manga) {
        val position = adapter.indexOf(manga)
        if (position != -1) {
            adapter.toggleSelection(position)
            (recycler.findViewHolderForItemId(manga.id!!) as? LibraryHolder)?.toggleActivation()
        }
    }

    /**
     * Tells the presenter to toggle the selection for the given position.
     *
     * @param position the position to toggle.
     */
    private fun toggleSelection(position: Int) {
        val item = adapter.getItem(position) ?: return

        controller.setSelection(item.manga, !adapter.isSelected(position))
        controller.invalidateActionMode()
    }

    /**
     * Called when a manga is clicked.
     *
     * @param position the position of the element clicked.
     * @return true if the item should be selected, false otherwise.
     */
    override fun onItemClick(view: View?, position: Int): Boolean {
        // If the action mode is created and the position is valid, toggle the selection.
        val item = adapter.getItem(position) ?: return false
        if (adapter.mode == SelectableAdapter.Mode.MULTI) {
            toggleSelection(position)
            return true
        } else {
            openManga(item.manga)
            return false
        }
    }

    /**
     * Called when a manga is long clicked.
     *
     * @param position
     */
    override fun onItemLongClick(position: Int) {
        controller.createActionModeIfNeeded()
        toggleSelection(position)
    }

    /**
     * Opens a manga.
     *
     * @param manga the manga to open.
     */
    private fun openManga(manga: Manga) {
        controller.openManga(manga)
    }
}
