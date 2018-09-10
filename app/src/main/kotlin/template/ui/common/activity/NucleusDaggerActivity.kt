package template.ui.common.activity

import android.support.v7.app.AppCompatActivity
import nucleus5.presenter.RxPresenter
import nucleus5.view.ViewWithPresenter

/**
 * Created by Robin Yeung on 9/10/18.
 */
abstract class NucleusDaggerActivity<P : RxPresenter<out Any>> : AppCompatActivity(), ViewWithPresenter<P> {

}