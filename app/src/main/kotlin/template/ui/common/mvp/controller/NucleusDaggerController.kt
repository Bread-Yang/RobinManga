package template.ui.common.mvp.controller

import android.os.Bundle
import android.support.annotation.CallSuper
import android.view.View
import com.bluelinelabs.conductor.Controller
import nucleus5.factory.PresenterFactory
import nucleus5.factory.ReflectionPresenterFactory
import nucleus5.presenter.RxPresenter
import nucleus5.view.ViewWithPresenter
import template.di.component.ControllerComponent
import template.di.module.ControllerModule
import template.ui.common.mvp.DaggerPresenterFactory
import template.ui.common.mvp.NucleusConductorDelegate
import template.ui.common.mvp.NucleusDaggerView
import template.ui.common.mvp.activity.NucleusDaggerActivity

abstract class NucleusDaggerController<P : RxPresenter<out Any>>(val bundle: Bundle? = null)
    : RxController(bundle), ViewWithPresenter<P>, NucleusDaggerView {

    // DI for the presenter
    private val presenterDelegate by lazy {
        NucleusConductorDelegate<P>(
                DaggerPresenterFactory<P, ReflectionPresenterFactory<P>>(
                        ReflectionPresenterFactory.fromViewClass<P>(javaClass)!!,
                        screenComponent()), this
        )
    }

    private val lifecycleListener = object : Controller.LifecycleListener() {

        override fun postCreateView(controller: Controller, view: View) {
            super.postCreateView(controller, view)

            onViewCreated(view)
            // here presenter will be created, or if rotate screen, event(onNext()、onError()、onComplete()) will be send again.
            presenterDelegate.onResume(this@NucleusDaggerController)
        }
    }

    init {
        addLifecycleListener(lifecycleListener)
    }

    abstract fun onViewCreated(view: View)

    @CallSuper
    override fun onDestroy() {
        presenterDelegate.onDestroy(true)
        removeLifecycleListener(lifecycleListener)
        super.onDestroy()
    }

    @CallSuper
    override fun onDestroyView(view: View) {
        super.onDestroyView(view)
        presenterDelegate.onDropView()
    }

    override fun getPresenter(): P = presenterDelegate.presenter

    override fun getPresenterFactory(): PresenterFactory<P> = presenterDelegate.presenterFactory!!

    override fun setPresenterFactory(presenterFactory: PresenterFactory<P>?) {
        presenterDelegate.presenterFactory = presenterFactory
    }

    private fun screenComponent(): ControllerComponent =
            (activity as NucleusDaggerActivity<*>).component().plus(ControllerModule(this))

}