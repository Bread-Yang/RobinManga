package template.ui.catalogue.global_search

import eu.davidea.flexibleadapter.FlexibleAdapter
import template.data.database.models.Manga

/**
 * Adapter that holds the manga items from search resultsPublicSubject.
 *
 * @param controller instance of [CatalogueSearchController].
 */
class CatalogueSearchCardAdapter(controller: CatalogueSearchController) :
        FlexibleAdapter<CatalogueSearchCardItem>(null, controller, true) {

    /**
     * Listen for browse item clicks.
     */
    val mangaClickListener: OnMangaClickListener = controller

    /**
     * Listener which should be called when user clicks browse.
     * Note: Should only be handled by [CatalogueSearchController]
     */
    interface OnMangaClickListener {
        fun onMangaClick(manga: Manga)
        fun onMangaLongClick(manga: Manga)
    }
}