package template.ui.common.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.ViewGroup
import com.bluelinelabs.conductor.Router
import template.annotation.Layout
import template.di.Injector
import template.di.component.ActivityComponent

/**
 * Not used.
 */
abstract class BaseActivity : AppCompatActivity() {

//    @Inject
    lateinit var router: Router

    private lateinit var component: ActivityComponent
    private lateinit var injector: Injector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout())
//        component = app.component.plus(ActivityModule(this, container(), savedInstanceState))
//        injector = Injector(component)
//        injector.inject(this)
    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }

    fun component() = component

    abstract fun container(): ViewGroup

    private fun layout(): Int {
        this.javaClass.kotlin.annotations.forEach {
            if (it is Layout)
                return it.layoutRes
        }
        throw IllegalArgumentException("You should specify Layout annotation")
    }
}
