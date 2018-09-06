package template.ui.common.mvp

import nucleus5.factory.PresenterFactory
import nucleus5.presenter.RxPresenter
import template.di.Injector
import template.ui.common.mvp.controller.NucleusController
import timber.log.Timber

class DaggerPresenterFactory<P : RxPresenter<out Any>,
        out PF : PresenterFactory<P>>(private val presenterFactory: PF,
                                      val component: Any, val controller: NucleusController<P>) : PresenterFactory<P> {

    private val injector: Injector = Injector(component)

    override fun createPresenter(): P {
        val presenter = presenterFactory.createPresenter()

        try {
            if (injector.isInjectable(presenter))
                injector.inject(presenter)
        } catch (exception: Exception) {
            Timber.e(exception, this.javaClass.simpleName)
        }

        return presenter
    }
}