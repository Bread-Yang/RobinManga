package template.di.component

import dagger.Subcomponent
import template.di.module.ControllerModule
import template.di.scopes.ControllerContext
import template.ui.catalogue.browse.BrowseCataloguePresenter
import template.ui.detail.DetailPresenter
import template.ui.main.MainPresenter
import template.ui.manga.MangaPresenter
import template.ui.manga.chapter.ChaptersPresenter
import template.ui.manga.info.MangaInfoPresenter

@ControllerContext
@Subcomponent(modules = arrayOf(ControllerModule::class))
interface ControllerComponent {

    fun inject(presenter: MainPresenter)

    fun inject(presenter: DetailPresenter)

    fun inject(presenter: BrowseCataloguePresenter)

    fun inject(presenter: MangaPresenter)

    fun inject(presenter: MangaInfoPresenter)

    fun inject(presenter: ChaptersPresenter)
}