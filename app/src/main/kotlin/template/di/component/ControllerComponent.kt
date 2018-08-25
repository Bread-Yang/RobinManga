package template.di.component

import dagger.Subcomponent
import template.di.module.ControllerModule
import template.di.scopes.ControllerContext
import template.ui.catalogue.browse.BrowseCataloguePresenter
import template.ui.detail.DetailPresenter
import template.ui.main.MainPresenter

@ControllerContext
@Subcomponent(modules = arrayOf(ControllerModule::class))
interface ControllerComponent {

    fun inject(presenter: MainPresenter)

    fun inject(presenter: DetailPresenter)

    fun inject(presenter: BrowseCataloguePresenter)
}