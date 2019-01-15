package template.ui.library

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.f2prateek.rx.preferences2.Preference
import com.jakewharton.rxbinding2.support.v4.view.pageSelections
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.library_controller.*
import nucleus5.factory.RequiresPresenter
import template.App
import template.R
import template.annotation.Layout
import template.data.database.models.Category
import template.data.database.models.Manga
import template.extensions.getOrDefault
import template.extensions.toast
import template.extensions.withFadeTransaction
import template.ui.common.mvp.controller.NucleusDaggerController
import template.ui.manga.MangaController
import timber.log.Timber
import java.io.IOException

/**
 * Created by Robin Yeung on 24/10/2018.
 */
@Layout(R.layout.library_controller)
@RequiresPresenter(LibraryPresenter::class)
class LibraryController : NucleusDaggerController<LibraryPresenter>(),
        ActionMode.Callback,
        ChangeMangaCategoriesDialog.Listener,
        DeleteLibraryMangasDialog.Listener {

    private companion object {
        /**
         * Key to change the cover of a manga in [onActivityResult].
         */
        const val REQUEST_IMAGE_OPEN = 101
    }

    private val preferencesHelper = App.app.lazyPreferencesHelper.get()

    /**
     * Position of the active category.
     */
    var activeCategory: Int = preferencesHelper.lastUsedCategory().getOrDefault()
        private set

    /**
     * Action mode for selections.
     */
    private var actionMode: ActionMode? = null

    /**
     * Library search query.
     */
    private var query = ""

    /**
     * Currently selected manags.
     */
    val selectedMangas = mutableSetOf<Manga>()

    private var selectedCoverManga: Manga? = null

    /**
     * Subject to notify the UI of selection updates.
     */
    val selectionUpdateSubject: PublishSubject<LibrarySelectionEvent> = PublishSubject.create()

    /**
     * Subject to notify search query changes.
     */
    val searchChangeSubject: BehaviorSubject<String> = BehaviorSubject.create()

    /**
     * Subject to notify the library's viewpager for updates.
     */
    val libraryMangaUpdateSubject: BehaviorSubject<LibraryMangaEvent> = BehaviorSubject.create()

    /**
     * Number of manga per row in grid mode.
     */
    var mangaPerRow = 0
        private set

    /**
     * Adapter of the view pager.
     */
    private var viewPagerAdapter: LibraryViewPagerAdapter? = null

    /**
     * Navigation view containing filter/sort/display items.
     */
    private var navView: LibraryNavigationView? = null

    /**
     * Drawer listener to allow swipe only for closing the drawer.
     */
    private var drawerListener: DrawerLayout.DrawerListener? = null

    private var tabsVisibilitySubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    private var tabVisibilityDispoable: Disposable? = null

    private var searchViewDisposable: Disposable? = null

    init {
        setHasOptionsMenu(true)
        retainViewMode = RetainViewMode.RETAIN_DETACH
    }

    override fun getTitle(): String? {
        return resources?.getString(R.string.label_library)
    }

    override fun onViewCreated(view: View) {
        viewPagerAdapter = LibraryViewPagerAdapter(this)
        libraryViewPager.adapter = viewPagerAdapter
        libraryViewPager
                .pageSelections()
                .skip(1)
                .subscribeUntilDestroy {
                    preferencesHelper.lastUsedCategory().set(it)
                    activeCategory = it
                }

        getColumnsPreferenceForCurrentOrientation().asObservable()
                .doOnNext {
                    mangaPerRow = it
                }
                .skip(1)
                // Set again the adapter to recalculate the covers height
                .subscribeUntilDestroy {
                    reattachAdapter()
                }

        if (selectedMangas.isNotEmpty()) {
            createActionModeIfNeeded()
        }
    }

    override fun initPresenterOnce() {

    }

    /**
     * Reattaches the adapter to the view pager to recreate fragments
     */
    private fun reattachAdapter() {
        val adapter = viewPagerAdapter ?: return

        val position = libraryViewPager.currentItem

        adapter.recycle = false
        libraryViewPager.adapter = adapter
        libraryViewPager.currentItem = position
        adapter.recycle = true
    }

    /**
     * Creates the action mode if it's not created already.
     */
    fun createActionModeIfNeeded() {
        if (actionMode == null) {
            actionMode = (activity as AppCompatActivity).startSupportActionMode(this)
        }
    }

    /**
     * Destorys the action mode.
     */
    fun destroyActionModeIfNeeded() {
        actionMode?.finish()
    }

    /**
     * Returns a preference for the number of manga per row based on the current orientation.
     *
     * @return the preference.
     */
    private fun getColumnsPreferenceForCurrentOrientation(): Preference<Int> {
        return if (resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            preferencesHelper.portraitColumns()
        } else {
            preferencesHelper.landscapeColumns()
        }
    }

    /**
     * Called when a filter is changed.
     */
    private fun onFilterChanged() {
        presenter.requestFilterUpdate()
        activity?.invalidateOptionsMenu()
    }

    private fun onDownloadBadgeChanged() {
        presenter.requestDownloadBadgesUpdate()
    }

    /**
     * Changes the cover for the selected manga.
     */
    private fun changeSelectedCover() {
        val manga = selectedMangas.firstOrNull() ?: return
        selectedCoverManga = manga

        if (manga.favorite) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, resources?.getString(R.string.file_select_cover)), REQUEST_IMAGE_OPEN)
        } else {
            activity?.toast(R.string.notification_first_add_to_library)
        }
    }

    /**
     * Move the selected manga to a list of categories.
     */
    private fun showChangeMangaCategoriesDialog() {
        // Create a copy of selected manga
        val mangas = selectedMangas.toList()

        // Hide the default category because it has a different behavior than the ones from db.
        val categories = presenter.categories.filter { it.id != 0 }

        // Get indexes of the common categories to preselect.
        val commonCategoriesIndexes = presenter.getCommonCategories(mangas)
                .map {
                    categories.indexOf(it)
                }
                .toTypedArray() // List<Int> 转成 Array<Int>, List和Array的区别 : https://stackoverflow.com/a/36263748

        ChangeMangaCategoriesDialog(this, mangas, categories, commonCategoriesIndexes)
                .showDialog(router)
    }

    private fun showDeleteMangaDialog() {
        DeleteLibraryMangasDialog(this, selectedMangas.toList()).showDialog(router)
    }

    override fun deleteMangasFromLibrary(mangas: List<Manga>, deleteChapters: Boolean) {
        presenter.removeMangaFromLibrary(mangas, deleteChapters)
        destroyActionModeIfNeeded()
    }

    override fun updateCategoriesForMangas(mangas: List<Manga>, categories: List<Category>) {
        presenter.moveMangasToCategories(categories, mangas)
        destroyActionModeIfNeeded()
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit_cover -> {
                changeSelectedCover()
                destroyActionModeIfNeeded()
            }
            R.id.action_move_to_category -> showChangeMangaCategoriesDialog()
            R.id.action_delete -> showDeleteMangaDialog()
            else -> return false
        }
        return true
    }

    /**
     * Invalidates the action mode, forcing it to refresh its content.
     */
    fun invalidateActionMode() {
        actionMode?.invalidate()
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
        mode.menuInflater.inflate(R.menu.library_selection, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        val count = selectedMangas.size
        if (count == 0) {
            // Destroy action mode if there are no items selected.
            destroyActionModeIfNeeded()
        } else {
            mode.title = resources?.getString(R.string.label_selected, count)
            menu.findItem(R.id.action_edit_cover)?.isVisible = count == 1
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        // Clear all the manga selections and notify child views.
        selectedMangas.clear()
        selectionUpdateSubject.onNext(LibrarySelectionEvent.Cleared())
        actionMode = null
    }

    fun openManga(manga: Manga) {
        // notify the presenter a manga is being opened.
        presenter.onOpenManga()

        router.pushController(MangaController(manga).withFadeTransaction())
    }

    /**
     * Sets the selection for a given manga.
     *
     * @param manga the manga whose selection has changed.
     * @param selected whether it's now selected or not.
     */
    fun setSelection(manga: Manga, selected: Boolean) {
        if (selected) {
            if (selectedMangas.add(manga)) {
                selectionUpdateSubject.onNext(LibrarySelectionEvent.Selected(manga))
            }
        } else {
            if (selectedMangas.remove(manga)) {
                selectionUpdateSubject.onNext(LibrarySelectionEvent.Unselected(manga))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_OPEN) {
            if (data == null || resultCode != Activity.RESULT_OK) return
            val activity = activity ?: return
            val manga = selectedCoverManga ?: return

            try {
                // Get the file's input stream from the incoming Intent
                activity.contentResolver.openInputStream(data.data).use {
                    // Update cover to selected file, show error if something went wrong
                    if (presenter.editCoverWithStream(it, manga)) {
                        // TODO REFRESH COVER
                    } else {
                        activity.toast(R.string.notification_cover_update_failed)
                    }
                }
            } catch (error: IOException) {
                activity.toast(R.string.notification_cover_update_failed)
                Timber.e(error)
            }
            selectedCoverManga = null
        }
    }
}