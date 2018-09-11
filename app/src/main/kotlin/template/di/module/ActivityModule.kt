package template.di.module

import android.app.Activity
import dagger.Module
import dagger.Provides
import template.di.scopes.ActivityContext

@Module
class ActivityModule(private val activity: Activity) {

    @Provides
    @ActivityContext
    fun provideActivityInstance() = activity
}