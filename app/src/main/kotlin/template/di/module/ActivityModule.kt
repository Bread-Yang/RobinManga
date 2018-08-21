package template.di.module

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import com.bluelinelabs.conductor.Conductor
import dagger.Module
import dagger.Provides
import template.di.scopes.ActivityContext

@Module
class ActivityModule(private val activity: Activity, private val container: ViewGroup, private val bundle: Bundle?) {

    @Provides
    @ActivityContext
    fun provideActivityInstance() = activity

    @Provides
    @ActivityContext
    fun provideRouter() = Conductor.attachRouter(activity, container, bundle)
}