package template.di.component

import android.app.Activity
import com.bluelinelabs.conductor.Router
import dagger.Subcomponent
import template.di.module.ActivityModule
import template.di.module.ControllerModule
import template.di.scopes.ActivityContext
import template.ui.MainActivity

@ActivityContext
@Subcomponent(modules = arrayOf(ActivityModule::class))
interface ActivityComponent {

    fun plus(controllerModule: ControllerModule): ControllerComponent

    fun inject(activity: MainActivity)

    fun activity(): Activity

    fun router(): Router
}