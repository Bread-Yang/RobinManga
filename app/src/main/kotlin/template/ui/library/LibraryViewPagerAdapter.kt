package template.ui.library

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import template.R
import template.data.database.models.Category
import template.extensions.inflate
import template.widget.RecyclerViewPagerAdapter

/**
 * This adapter stores the categories from the library, used with a ViewPager.
 *
 * @constructor creates an instance of the adapter.
 */
class LibraryViewPagerAdapter(private val controller: LibraryController) : RecyclerViewPagerAdapter() {

    /**
     * The categories to bind in the adapter.
     */
    var categories: List<Category> = emptyList()
        // This setter helps to not refresh the adapter if the reference to the list doesn't change.
        set(value) {
            if (field !== value) {
                field = value
                notifyDataSetChanged()
            }
        }

    private var boundViews = arrayListOf<View>()

    /**
     * Creates a new view for this adapter.
     *
     * @return a new view.
     */
    override fun createView(container: ViewGroup): View {
        val view = container.inflate(R.layout.library_category) as LibraryCategoryView
        view.onCreate(controller)
        return view
    }

    /**
     * Binds a view with a position.
     *
     * @param view the view to bind.
     * @param position the position in the adapter.
     */
    override fun bindView(view: View, position: Int) {
        (view as LibraryCategoryView).onBind(categories[position])
        boundViews.add(view)
    }

    override fun recycleView(view: View, position: Int) {
        (view as LibraryCategoryView).onRecycle()
        boundViews.remove(view)
    }

    /**
     * Returns the number of categories.
     *
     * @return the number of categories or 0 if the list is null.
     */
    override fun getCount(): Int {
        return categories.size
    }

    /**
     * Returns the title to display for a category.
     *
     * @param position the position of the element.
     * @return the title to display.
     */
    override fun getPageTitle(position: Int): CharSequence? {
        return categories[position].name
    }

    override fun getItemPosition(obj: Any): Int {
        val view = obj as? LibraryCategoryView ?: return PagerAdapter.POSITION_NONE
        val index = categories.indexOfFirst { it.id == view.category.id }
        return if (index == -1) PagerAdapter.POSITION_NONE else index
    }

    /**
     * Called when the view of this adapter is being destroyed.
     */
    fun onDestroy(){
        for (view in boundViews) {
            if (view is LibraryCategoryView) {
                view.disposable()
            }
        }
    }
}
