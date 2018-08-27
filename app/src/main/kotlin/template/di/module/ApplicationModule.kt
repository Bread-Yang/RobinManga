package template.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import template.App
import template.data.cache.CoverCache
import template.data.database.DatabaseHelper
import template.di.scopes.ApplicationContext
import template.network.NetworkHelper
import template.source.SourceManager
import template.utils.preference.PreferencesHelper
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: App) {

    @Provides
    @Singleton
    @ApplicationContext
    fun provideApplicationContext(): Context = application.applicationContext

    @Provides
    @Singleton
    @ApplicationContext
    fun providePreferencesHelper(context: Context): PreferencesHelper = PreferencesHelper(context)

    @Provides
    @Singleton
    @ApplicationContext
    fun provideNetworkHelper(context: Context): NetworkHelper = NetworkHelper(context)

    @Provides
    @Singleton
    @ApplicationContext
    fun provideDatabaseHelper(context: Context): DatabaseHelper = DatabaseHelper(context)

    @Provides
    @Singleton
    @ApplicationContext
    fun provideCoverCache(context: Context): CoverCache = CoverCache(context)

    @Provides
    @Singleton
    @ApplicationContext
    fun provideSourceManager(context: Context, networkHelper: NetworkHelper): SourceManager = SourceManager(context, networkHelper)
}
