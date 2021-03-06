package template.di.component

import dagger.Subcomponent
import template.di.module.ControllerModule
import template.di.scopes.ControllerContext
import template.ui.catalogue.browse.BrowseCataloguePresenter
import template.ui.catalogue.global_search.CatalogueSearchPresenter
import template.ui.download.DownloadPresenter
import template.ui.library.LibraryPresenter
import template.ui.manga.MangaPresenter
import template.ui.manga.chapter.ChaptersPresenter
import template.ui.manga.info.MangaInfoPresenter
import template.ui.recently_read.RecentlyReadPresenter

@ControllerContext
@Subcomponent(modules = arrayOf(ControllerModule::class))
interface ControllerComponent {

    fun inject(presenter: BrowseCataloguePresenter)

    fun inject(presenter: MangaPresenter)

    fun inject(presenter: MangaInfoPresenter)

    fun inject(presenter: ChaptersPresenter)

    fun inject(presenter: DownloadPresenter)

    fun inject(presenter: LibraryPresenter)

    fun inject(presenter: CatalogueSearchPresenter)

    fun inject(presenter: RecentlyReadPresenter)
}