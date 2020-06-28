package template.ui.common.mvp.activity

import android.os.Bundle
import androidx.annotation.CallSuper
import android.view.View
import nucleus5.factory.PresenterFactory
import nucleus5.factory.ReflectionPresenterFactory
import nucleus5.presenter.RxPresenter
import nucleus5.view.ViewWithPresenter
import template.annotation.Layout
import template.di.component.ActivityComponent
import template.di.module.ActivityModule
import template.extensions.app
import template.ui.common.mvp.DaggerPresenterFactory
import template.ui.common.mvp.NucleusConductorDelegate
import template.ui.common.mvp.NucleusDaggerView

/**
 * Created by Robin Yeung on 9/10/18.
 */
abstract class NucleusDaggerActivity<P : RxPresenter<out Any>>
    : RxActivity(), ViewWithPresenter<P>, NucleusDaggerView {

    private val PRESENTER_STATE_KEY = "presenter_state"

    private val component: ActivityComponent by lazy {
        app.component.plus(ActivityModule(this))
    }

    // DI for the presenter
    private val presenterDelegate: NucleusConductorDelegate<P> by lazy {
        NucleusConductorDelegate<P>(
                DaggerPresenterFactory<P, ReflectionPresenterFactory<P>>(
                        ReflectionPresenterFactory.fromViewClass<P>(javaClass)!!,
                        component
                ), this
        )
    }

    fun component() = component

    override fun getPresenter(): P = presenterDelegate.presenter

    override fun getPresenterFactory(): PresenterFactory<P> = presenterDelegate.presenterFactory!!

    override fun setPresenterFactory(presenterFactory: PresenterFactory<P>?) {
        presenterDelegate.presenterFactory = presenterFactory
    }

    final override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
    }

    final override fun setContentView(view: View?) {
        super.setContentView(view)
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout())
        if (savedInstanceState != null) {
            presenterDelegate.onRestoreInstanceState(savedInstanceState.getBundle(PRESENTER_STATE_KEY))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(PRESENTER_STATE_KEY, presenterDelegate.onSaveInstanceState())
    }

    override fun onResume() {
        super.onResume()
        presenterDelegate.onResume(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenterDelegate.onDropView()
        presenterDelegate.onDestroy(!isChangingConfigurations)
    }

    private fun layout(): Int {
        this.javaClass.kotlin.annotations.forEach {
            if (it is Layout)
                return it.layoutRes
        }
        throw IllegalArgumentException("You should specify Layout annotation")
    }
}