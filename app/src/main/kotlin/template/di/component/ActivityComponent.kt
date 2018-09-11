package template.di.component

import dagger.Subcomponent
import template.di.module.ActivityModule
import template.di.module.ControllerModule
import template.di.scopes.ActivityContext
import template.ui.main.MainPresenter
import template.ui.reader.ReaderPresenter

@ActivityContext
@Subcomponent(modules = arrayOf(ActivityModule::class))
interface ActivityComponent {

    fun plus(controllerModule: ControllerModule): ControllerComponent

    fun inject(presenter: MainPresenter)

    fun inject(presenter: ReaderPresenter)
}