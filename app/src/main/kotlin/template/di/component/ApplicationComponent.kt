package template.di.component

import dagger.Component
import template.App
import template.di.module.ActivityModule
import template.di.module.ApiModule
import template.di.module.ApplicationModule
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(ApplicationModule::class, ApiModule::class))
interface ApplicationComponent {

    fun inject(app: App)

    fun plus(activityModule: ActivityModule): ActivityComponent
}